---
name: android-koin
description: Configurar inyección de dependencias Koin, módulos, anotaciones KSP y scopes Android. Usar al registrar implementaciones o resolver fallos del grafo de dependencias.
---

# Configurar Koin

## Convencion de esta biblioteca

Usa `AndroidApplication` como nombre generico. Su `onCreate` llama a `koinInitializer()`, que ejecuta `startKoin`, `androidContext(this@AndroidApplication)`, `androidLogger()` y `modules(AppModule().module)`.

`AppModule` es una clase `@Module(includes = [...])` y `@ComponentScan`; incluye los modulos reales de infraestructura y features. Cada feature expone `<Feature>Module` con `@Module` y `@ComponentScan` en su paquete raiz. Usa `@KoinViewModel` para ViewModels y `@Single` para proveedores compartidos cuando corresponda; no registres ViewModels como singletons.

Centraliza KSP, annotations y compiler en `plugin.koin`, aplicado en app, infraestructura que lo consuma y `plugin.feature`. Usa `KOIN_DEFAULT_MODULE=false` e importa `org.koin.ksp.generated.module` al cargar el modulo generado. No mantengas registros DSL duplicados. No crees modulos vacios ni añadas Firebase o HTTP solo por aparecer en la referencia.

Si la plantilla selecciona demo/Firebase por configuracion de build, conserva esa seleccion y evita inicializar SDKs remotos en demo. Las dependencias exclusivas de Firebase pueden resolverse de forma diferida. Conserva los callbacks de cierre existentes al migrar DSL; cuando una anotacion no pueda representarlos, usa un mecanismo de lifecycle explicito y documentado.

## Procedimiento

1. Inspecciona si el proyecto usa DSL o anotaciones. Mantén un enfoque coherente y verifica compatibilidad entre Koin, annotations, compiler y KSP.
2. Declara módulos por propietario: infraestructura, feature y composición de app. Vincula interfaces de dominio con implementaciones en el borde externo.
3. Define singleton para servicios compartidos seguros, factory para instancias independientes y ViewModel/scope para estado con dueño de lifecycle.
4. Si usas anotaciones, configura KSP y verifica imports/módulos generados. No edites código generado ni crees registros duplicados entre DSL y escaneo.
5. Inicializa Koin una vez en Application, con contexto Android solo para adaptadores que lo necesitan. Evita dependencias de Activity en singletons.
6. Para estado compartido entre pantallas, usa el mismo propietario de grafo explícito; no conviertas el ViewModel en singleton global.

## Comprobar y entregar

Compila el código generado y resuelve los puntos de entrada reales. Verifica el grafo mediante APIs compatibles con la versión instalada y pruebas con implementaciones controladas. Comprueba que logout cierre recursos asociados a sesión.
