# Taller #1 — Ecommerce: lista y visualización de productos (Jetpack Compose)

Aplicación Android que consulta el API `https://dummyjson.com/products?limit=194`,
muestra los productos en una cuadrícula y permite abrir una vista de detalle con
la información ampliada de cada producto.

## Librerías exigidas

| Requisito            | Librería / versión              |
|----------------------|---------------------------------|
| UI                   | Jetpack Compose + Kotlin        |
| Navegación           | Navigation 3 `1.1.6`            |
| Networking           | Ktor `3.5.2` (motor OkHttp)     |
| Imágenes             | Coil `3.5.0`                    |
| Serialización JSON   | kotlinx.serialization `1.11.0`  |

## Estructura del proyecto

```
app/src/main/java/com/example/taller_1/
├── MainActivity.kt              Punto de entrada. Aplica el tema y el host de navegación.
├── data/
│   ├── model/
│   │   └── Product.kt           data class del producto (y Dimensions, Review, ProductsResponse).
│   └── remote/
│       └── ProductApi.kt        Cliente Ktor. Única consulta al API.
├── navigation/
│   ├── AppRoutes.kt             Rutas NavKey (@Serializable). La ruta de detalle lleva el objeto Product.
│   └── AppNavHost.kt            NavDisplay + entryProvider. Carga la lista una sola vez.
├── ui/
│   ├── components/
│   │   ├── ProductCard.kt       Componente reutilizable para cada ítem de la cuadrícula.
│   │   └── DetailSection.kt     Componente reutilizable para cada campo de la pantalla de detalle.
│   ├── screens/
│   │   ├── ProductListScreen.kt LazyVerticalGrid + StickyHeader con el total de productos.
│   │   └── ProductDetailScreen.kt  Muestra 7 campos del JSON y dispara los intents.
│   └── theme/                   Color.kt, Theme.kt, Type.kt. Dynamic Color + tema claro y oscuro.
└── util/
    └── ProductIntents.kt        Intents ACTION_DIAL (título) y ACTION_SEND "smsto:" (descripción).
```

## Flujo

1. `AppNavHost` ejecuta **una sola vez** `ProductApi.getProducts()` (endpoint
   `products?limit=194`) y guarda la lista de los 194 productos en estado.
2. `ProductListScreen` dibuja la cuadrícula de dos columnas con `ProductCard`
   (nombre, categoría, precio, calificación e imagen). El `StickyHeader` muestra
   el número total de productos.
3. Al tocar una tarjeta se navega a `ProductDetailScreen` pasando el objeto
   `Product` completo por la ruta.
4. En el detalle:
   - tocar el **título** ejecuta `Intent.ACTION_DIAL` con el id del producto;
   - tocar la **descripción** ejecuta `Intent.ACTION_SEND` con la forma `smsto:`
     y el cuerpo `"title: description"`.

## Tema (Dynamic Color / Material You)

`AppTheme` selecciona el esquema de color así:

- **Android 12 (API 31) o superior**: usa Dynamic Color, tomando los colores del
  fondo de pantalla del dispositivo con `dynamicLightColorScheme` /
  `dynamicDarkColorScheme`.
- **Versiones anteriores**: usa la paleta de respaldo definida en `Color.kt`.

En ambos casos se respeta el modo claro u oscuro del sistema
(`isSystemInDarkTheme`). El parámetro `dynamicColor` de `AppTheme` permite
desactivar Material You si se desea forzar la paleta propia.

## Compilación

```bash
./gradlew :app:assembleDebug
```

El APK de depuración queda en `app/build/outputs/apk/debug/`.
