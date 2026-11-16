package com.example.tcg_manager.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.tcg_manager.R
import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.data.model.CardGame
import com.example.tcg_manager.data.model.SearchType
import com.example.tcg_manager.data.repository.SearchOutcome
import com.example.tcg_manager.databinding.FragmentScanBinding
import com.example.tcg_manager.ui.card.CardDetailFragment
import com.example.tcg_manager.util.rotated
import com.example.tcg_manager.util.scaledDownTo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/** Start screen: scan a card with the camera, or search manually by id / name / description. */
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanViewModel by activityViewModels { ScanViewModel.Factory }

    private var imageCapture: ImageCapture? = null

    private val manualSearchTypes = SearchType.entries.filter { it != SearchType.IMAGE }

    private val cameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else showStatus(getString(R.string.scan_camera_permission_needed))
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gameSpinner.adapter = spinnerAdapter(
            listOf(getString(R.string.scan_game_auto)) + CardGame.entries.map { it.displayName }
        )
        binding.searchTypeSpinner.adapter = spinnerAdapter(manualSearchTypes.map { it.displayName })
        binding.searchTypeSpinner.setSelection(manualSearchTypes.indexOf(SearchType.NAME))

        binding.scanButton.setOnClickListener { captureAndSearch() }
        binding.searchButton.setOnClickListener { manualSearch() }
        binding.queryInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { manualSearch(); true } else false
        }

        if (hasCameraPermission()) startCamera() else cameraPermission.launch(Manifest.permission.CAMERA)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::render)
            }
        }
    }

    private fun render(state: ScanViewModel.State) {
        val loading = state is ScanViewModel.State.Loading
        binding.progress.isVisible = loading
        binding.scanButton.isEnabled = !loading
        binding.searchButton.isEnabled = !loading
        when (state) {
            ScanViewModel.State.Idle -> Unit
            is ScanViewModel.State.Loading -> showStatus(state.message)
            is ScanViewModel.State.Done -> {
                viewModel.resultHandled()
                handleOutcome(state.outcome)
            }
        }
    }

    private fun handleOutcome(outcome: SearchOutcome) {
        val read = outcome.ocr?.nameCandidates?.takeIf { it.isNotEmpty() }
            ?.let { getString(R.string.scan_read_names, it.joinToString(" / ")) }
        val problems = outcome.errors.joinToString("\n").ifBlank { null }

        when (outcome.cards.size) {
            0 -> showStatus(listOfNotNull(getString(R.string.scan_no_results), read, problems).joinToString("\n"))
            1 -> {
                showStatus(listOfNotNull(read, problems).joinToString("\n"))
                openCard(outcome.cards.single())
            }
            else -> {
                showStatus(listOfNotNull(read, problems).joinToString("\n"))
                chooseCard(outcome.cards)
            }
        }
    }

    private fun chooseCard(cards: List<Card>) {
        val labels = cards.map { card ->
            listOfNotNull(card.name, card.setName ?: card.setCode, card.collectorNumber?.let { "#$it" })
                .joinToString(" · ")
        }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.scan_choose_card, cards.size))
            .setItems(labels) { _, which -> openCard(cards[which]) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openCard(card: Card) {
        viewModel.select(card)
        findNavController().navigate(
            R.id.action_scan_to_cardDetail,
            bundleOf(CardDetailFragment.ARG_ASK_TO_ADD to true),
        )
    }

    private fun manualSearch() {
        val type = manualSearchTypes[binding.searchTypeSpinner.selectedItemPosition]
        viewModel.search(type, binding.queryInput.text?.toString().orEmpty(), selectedGame())
    }

    private fun selectedGame(): CardGame? =
        binding.gameSpinner.selectedItemPosition.let { if (it <= 0) null else CardGame.entries[it - 1] }

    // --- Camera -----------------------------------------------------------------------------

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val context = requireContext()
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val binding = _binding ?: return@addListener
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            try {
                provider.unbindAll()
                provider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                imageCapture = capture
            } catch (e: Exception) {
                showStatus(getString(R.string.scan_camera_error, e.message))
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun captureAndSearch() {
        val capture = imageCapture
        if (capture == null) {
            if (!hasCameraPermission()) cameraPermission.launch(Manifest.permission.CAMERA)
            else showStatus(getString(R.string.scan_camera_not_ready))
            return
        }
        val game = selectedGame()
        capture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.use {
                        it.toBitmap().scaledDownTo(MAX_OCR_SIDE).rotated(it.imageInfo.rotationDegrees)
                    }
                    viewModel.searchByImage(bitmap, game)
                }

                override fun onError(exception: ImageCaptureException) {
                    showStatus(getString(R.string.scan_camera_error, exception.message))
                }
            },
        )
    }

    // --- Helpers ----------------------------------------------------------------------------

    private fun spinnerAdapter(items: List<String>) =
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

    private fun showStatus(text: String) {
        _binding?.statusText?.apply {
            this.text = text
            isVisible = text.isNotBlank()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imageCapture = null
        _binding = null
    }

    private companion object {
        const val MAX_OCR_SIDE = 1600
    }
}
