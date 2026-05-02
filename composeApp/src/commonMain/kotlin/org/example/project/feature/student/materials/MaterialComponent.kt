package org.example.project.feature.student.materials

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.core.network.Result
import org.example.project.domain.models.Material
import org.example.project.domain.repository.MaterialRepository
import org.example.project.domain.utils.FileSaver
import org.example.project.feature.student.materials.ui.GroupChip

class MaterialComponent(
    componentContext: ComponentContext,
    private val repository: MaterialRepository,
    private val fileSaver: FileSaver,
    private val settings: Settings
) : ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val _state = MutableStateFlow(MaterialState())
    val state: StateFlow<MaterialState> = _state.asStateFlow()

    init {
        loadMaterials()
    }

    fun onSearchChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        loadMaterials()
    }

    fun onGroupSelected(groupId: String?) {
        _state.update { it.copy(selectedGroupId = groupId) }
        loadMaterials()
    }

    fun downloadMaterial(material: Material) {
        scope.launch {
            _state.update { it.copy(downloadingId = material.id) }
            when (val result = repository.downloadMaterial(material.id)) {
                is Result.Success -> {
                    fileSaver.saveFile(result.data, material.originalFileName)
                    _state.update { it.copy(downloadingId = null, message = "✅ Сохранено: ${material.originalFileName}") }
                }
                is Result.Error -> {
                    _state.update { it.copy(downloadingId = null, message = "❌ Не удалось скачать файл") }
                }
            }
        }
    }

    fun previewMaterial(material: Material) {
        scope.launch {
            _state.update { it.copy(downloadingId = material.id) }
            when (val result = repository.previewMaterial(material.id)) {
                is Result.Success -> {
                    // Для изображений — сохраняем и показываем превью
                    if (material.isImage) {
                        _state.update { it.copy(downloadingId = null, previewBytes = result.data, previewMaterial = material) }
                    } else {
                        // Для остальных файлов — скачиваем
                        fileSaver.saveFile(result.data, material.originalFileName)
                        _state.update { it.copy(downloadingId = null, message = "✅ Открыто: ${material.originalFileName}") }
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(downloadingId = null, message = "❌ Не удалось открыть превью") }
                }
            }
        }
    }

    fun closePreview() {
        _state.update { it.copy(previewBytes = null, previewMaterial = null) }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun loadMaterials() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val orgId = settings.getString("orgId", "")
            val current = _state.value
            when (val result = repository.getMaterials(
                organizationId = orgId,
                groupId = current.selectedGroupId,
                searchTitle = current.searchQuery.takeIf { it.isNotBlank() }
            )) {
                is Result.Success -> _state.update { it.copy(isLoading = false, materials = result.data) }
                is Result.Error -> _state.update { it.copy(isLoading = false, error = "Ошибка загрузки материалов") }
            }
        }
    }
}

data class MaterialState(
    val groups: List<GroupChip> = emptyList(),
    val isLoading: Boolean = false,
    val materials: List<Material> = emptyList(),
    val searchQuery: String = "",
    val selectedGroupId: String? = null,
    val downloadingId: String? = null,
    val previewBytes: ByteArray? = null,
    val previewMaterial: Material? = null,
    val message: String? = null,
    val error: String? = null
)