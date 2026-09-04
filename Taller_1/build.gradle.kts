// Archivo de construcción de nivel superior. Aquí se declaran los plugins que
// estarán disponibles para los módulos del proyecto (en este caso, solo :app).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
