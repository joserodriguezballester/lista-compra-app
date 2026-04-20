package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import com.jose.listacompra.domain.storage.ArticuloPhotoStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticuloPhotoCentralizationUseCaseTest {

    @Test
    fun `saveArticulo centralizes non blank photo before saving`() = runBlocking {
        val repository = FakeArticuloRepository()
        val photoStorage = FakeArticuloPhotoStorage(
            nextResult = "content://media/external/images/media/100"
        )
        val useCase = SaveArticuloUseCase(repository, photoStorage)

        val result = useCase(
            articulo(
                id = 1L,
                photoUri = "content://temp/new-photo"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals(
            listOf(CentralizeRequest("content://temp/new-photo", "Tomate frito")),
            photoStorage.requests
        )
        assertEquals(
            "content://media/external/images/media/100",
            repository.savedArticulo?.photoUri
        )
    }

    @Test
    fun `saveArticulo skips storage when photo is blank`() = runBlocking {
        val repository = FakeArticuloRepository()
        val photoStorage = FakeArticuloPhotoStorage()
        val useCase = SaveArticuloUseCase(repository, photoStorage)

        val result = useCase(
            articulo(
                id = 2L,
                photoUri = "   "
            )
        )

        assertTrue(result.isSuccess)
        assertTrue(photoStorage.requests.isEmpty())
        assertNull(repository.savedArticulo?.photoUri)
    }

    @Test
    fun `updateArticulo keeps existing photo when it did not change`() = runBlocking {
        val repository = FakeArticuloRepository().apply {
            articlesById[7L] = articulo(
                id = 7L,
                photoUri = "content://legacy/existing-photo"
            )
        }
        val photoStorage = FakeArticuloPhotoStorage(
            nextResult = "content://media/external/images/media/777"
        )
        val useCase = UpdateArticuloUseCase(repository, photoStorage)

        val result = useCase(
            articulo(
                id = 7L,
                photoUri = "content://legacy/existing-photo"
            )
        )

        assertTrue(result.isSuccess)
        assertTrue(photoStorage.requests.isEmpty())
        assertEquals(
            "content://legacy/existing-photo",
            repository.updatedArticulo?.photoUri
        )
    }

    @Test
    fun `updateArticulo centralizes changed photo before saving`() = runBlocking {
        val repository = FakeArticuloRepository().apply {
            articlesById[9L] = articulo(
                id = 9L,
                photoUri = "content://legacy/old-photo"
            )
        }
        val photoStorage = FakeArticuloPhotoStorage(
            nextResult = "content://media/external/images/media/999"
        )
        val useCase = UpdateArticuloUseCase(repository, photoStorage)

        val result = useCase(
            articulo(
                id = 9L,
                photoUri = "https://images.example.com/new-photo.jpg"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals(
            listOf(CentralizeRequest("https://images.example.com/new-photo.jpg", "Tomate frito")),
            photoStorage.requests
        )
        assertEquals(
            "content://media/external/images/media/999",
            repository.updatedArticulo?.photoUri
        )
    }

    @Test
    fun `updateArticulo clears photo without calling storage when photo becomes null`() = runBlocking {
        val repository = FakeArticuloRepository().apply {
            articlesById[11L] = articulo(
                id = 11L,
                photoUri = "content://legacy/photo-to-remove"
            )
        }
        val photoStorage = FakeArticuloPhotoStorage()
        val useCase = UpdateArticuloUseCase(repository, photoStorage)

        val result = useCase(
            articulo(
                id = 11L,
                photoUri = null
            )
        )

        assertTrue(result.isSuccess)
        assertTrue(photoStorage.requests.isEmpty())
        assertNull(repository.updatedArticulo?.photoUri)
    }

    @Test
    fun `updateArticulo fails when target article does not exist`() = runBlocking {
        val repository = FakeArticuloRepository()
        val photoStorage = FakeArticuloPhotoStorage()
        val useCase = UpdateArticuloUseCase(repository, photoStorage)

        val result = useCase(
            articulo(
                id = 99L,
                photoUri = "content://temp/missing"
            )
        )

        assertTrue(result.isFailure)
        assertTrue(photoStorage.requests.isEmpty())
        assertNull(repository.updatedArticulo)
    }

    private fun articulo(
        id: Long = 0L,
        photoUri: String? = null
    ) = Articulo(
        id = id,
        name = "Tomate frito",
        finalPrice = 1.95f,
        photoUri = photoUri,
        ean = "1234567890123",
        categoryId = 3L,
        size = 1f,
        unit = "ud"
    )

    private class FakeArticuloRepository : IArticuloRepository {
        val articlesById = mutableMapOf<Long, Articulo>()
        var savedArticulo: Articulo? = null
        var updatedArticulo: Articulo? = null

        override fun getAllArticulos(): Flow<List<Articulo>> = flowOf(emptyList())

        override suspend fun getArticuloByEan(ean: String): Articulo? = null

        override suspend fun getArticuloById(id: Long): Articulo? = articlesById[id]

        override suspend fun getArticulosCount(): Int = articlesById.size

        override suspend fun searchArticulos(query: String): List<Articulo> = emptyList()

        override suspend fun saveArticulo(articulo: Articulo) {
            savedArticulo = articulo
            if (articulo.id > 0) {
                articlesById[articulo.id] = articulo
            }
        }

        override suspend fun saveAll(articulos: List<Articulo>) {
            articulos.forEach { saveArticulo(it) }
        }

        override suspend fun deleteArticulo(articulo: Articulo) {
            articlesById.remove(articulo.id)
        }

        override suspend fun updateArticulo(articulo: Articulo) {
            updatedArticulo = articulo
            articlesById[articulo.id] = articulo
        }
    }

    private class FakeArticuloPhotoStorage(
        private val nextResult: String = "content://media/external/images/media/fake"
    ) : ArticuloPhotoStorage {
        val requests = mutableListOf<CentralizeRequest>()

        override suspend fun centralizeIfNeeded(photoUri: String, articuloName: String): String {
            requests += CentralizeRequest(photoUri, articuloName)
            return nextResult
        }
    }

    private data class CentralizeRequest(
        val photoUri: String,
        val articuloName: String
    )
}
