package com.jose.listacompra.ui.screens.main.components

//import androidx.compose.material3.rememberDismissState

//fun SwipeableProductCard(
//    product: Product,
//    offer: Offer?,
//    onTogglePurchased: () -> Unit,
//    onDelete: () -> Unit,
//    onEdit: () -> Unit
//) {
//    val dismissState = rememberDismissState(
//        confirmValueChange = { dismissValue ->
//            if (dismissValue == DismissValue.DismissedToStart) {
//                onDelete()
//                true
//            } else {
//                false
//            }
//        }
//    )
//
//    SwipeToDismiss(
//        state = dismissState,
//        directions = setOf(DismissDirection.EndToStart),
//        background = {
//            val color by animateColorAsState(
//                when (dismissState.targetValue) {
//                    DismissValue.Default -> Color.Companion.Transparent
//                    DismissValue.DismissedToEnd -> Color.Companion.Green.copy(alpha = 0.3f)
//                    DismissValue.DismissedToStart -> Color.Companion.Red.copy(alpha = 0.3f)
//                },
//                label = "background color"
//            )
//
//            Box(
//                modifier = Modifier.Companion
//                    .fillMaxSize()
//                    .background(color)
//                    .padding(horizontal = 20.dp),
//                contentAlignment = Alignment.Companion.CenterEnd
//            ) {
//                Icon(
//                    Icons.Default.Delete,
//                    contentDescription = "Eliminar",
//                    tint = MaterialTheme.colorScheme.error
//                )
//            }
//        },
//        dismissContent = {
//            ProductCard(
//                product = product,
//                offer = offer,
//                onTogglePurchased = onTogglePurchased,
//                onEdit = onEdit
//            )
//        }
//    )
//}