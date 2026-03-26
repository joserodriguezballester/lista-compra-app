package com.jose.listacompra.data.local.dataseeder

data class SeedArticulo(val name: String,
                        val categoryId: Long,
                        val finalPrice: Float? = null,
                        val size: Float = 1f,
                        val unit: String = "ud",
                        val ean: String? = null,
                        val photoUri: String? = null)
