# Navegación actual de `lista-compra-app`

## Resumen

La navegación principal de la app está centralizada en `AppNavigation.kt` y se apoya en un `AppNavigator` simple para las acciones del drawer. La idea práctica es que las pantallas principales compartan el mismo armazón visual (`drawer` + `top bar` + `scaffold`) a través de `AppDrawerScaffold`.

## Piezas principales

- **`app/src/main/java/com/jose/listacompra/ui/navigation/AppNavigation.kt`**
  - define el `NavHost` principal;
  - usa `Splash` como `startDestination`;
  - crea un `AppNavigatorImpl` y lo pasa a las pantallas que participan del drawer;
  - centraliza las rutas principales y algunos helpers de navegación a `Home` y `Mi Lista`.

- **`app/src/main/java/com/jose/listacompra/ui/navigation/AppNavigator.kt`**
  - interfaz mínima para navegar a un `DrawerDestination`.

- **`app/src/main/java/com/jose/listacompra/ui/navigation/AppNavigatorImpl.kt`**
  - traduce cada `DrawerDestination` a una navegación real con `NavHostController`;
  - usa `launchSingleTop` en los destinos del drawer;
  - para `Home` y `ShoppingList` aplica además `popUpTo` para evitar acumulación rara de backstack.

- **`app/src/main/java/com/jose/listacompra/ui/navigation/DrawerDestination.kt`**
  - define los destinos visibles del drawer:
    - `Home`
    - `ShoppingList`
    - `Catalog`
    - `Categories`
    - `Offers`
    - `Supermarkets`
    - `History`
    - `TicketImport`
  - mantiene también `DrawerDestination.all` como lista común de entradas.

- **`app/src/main/java/com/jose/listacompra/ui/components/AppDrawerScaffold.kt`**
  - actúa como armazón común para las pantallas principales;
  - combina `ModalNavigationDrawer` + `Scaffold` + `CommonTopBar`;
  - resuelve navegación por drawer usando `AppNavigator` cuando está disponible, o callbacks directos si no lo está;
  - mantiene una sola forma de abrir/cerrar drawer y pintar el top bar compartido.

- **`app/src/main/java/com/jose/listacompra/ui/components/AppDrawer.kt`**
  - renderiza el drawer;
  - muestra `Home` de forma explícita y el resto de entradas a partir de `DrawerDestination.all`;
  - marca el destino actual cuando se conoce;
  - deja el nombre de versión en la parte inferior.

## Rutas principales actuales

En el `NavHost` actual aparecen como piezas relevantes:

- `Splash`
- `Home`
- `ShoppingList`
- `Catalogo`
- `Offers`
- `Categories`
- `History`
- `BarcodeScanner`
- `TicketImport`
- `Supermarkets`
- `SupermarketAisles/{supermarketId}`

## Pantallas integradas en el armazón común

A nivel práctico, el bloque de navegación anterior dejó integradas en el esquema común del drawer las pantallas principales que reciben `navigator = appNavigator`, entre ellas:

- `HomeScreen`
- `ProductListScreen`
- `CatalogoScreen`
- `OffersScreen`
- `CategoriesScreen`
- `HistoryScreen`
- `SupermarketListScreen`
- `TicketImportScreen`

## Casos especiales a recordar

- **`TicketImportScreen`** ya quedó integrada en el armazón común de navegación y drawer.
- **`BarcodeScannerScreen`** no forma parte del drawer; se usa como ruta puntual y vuelve con `popBackStack()`.
- **`SupermarketAislesScreen`** cuelga de `Supermarkets` y recibe `supermarketId` por argumento de navegación.
- La navegación a `Home` y `Mi Lista` está más protegida frente a acumulación de destinos por el uso de `popUpTo`.

## Validación mínima útil

Existe al menos este test instrumental ligado al drawer:

- **`app/src/androidTest/java/com/jose/listacompra/ui/components/AppDrawerDestinationTest.kt`**
  - comprueba que `Importar Ticket` emite `DrawerDestination.TicketImport`;
  - comprueba que el destino actual se marca como seleccionado;
  - comprueba navegación básica a `Home` y `Mi Lista`.

## Idea guía

Cuando se toque la navegación de la app, revisar primero:
- `AppNavigation.kt`
- `AppNavigatorImpl.kt`
- `DrawerDestination.kt`
- `AppDrawerScaffold.kt`
- `AppDrawer.kt`

y después las pantallas integradas que usan `navigator`.
