# Início da Milestone 2 - Membro 1

A tua primeira tarefa de acordo com o plano de projeto é criar as fundações arquitetónicas da aplicação, especificamente para o *Settings*, *About*, e a persistência da *API Key* (BYOK) utilizando o `DataStore`. Para além disso, criarás o Serviço de Localização (*Service Locator*) na classe `Application`.

## Proposed Changes

Vou implementar as tuas tarefas da Milestone 2 estruturadas da seguinte forma:

### Dependências
#### [MODIFY] `gradle/libs.versions.toml`
#### [MODIFY] `app/build.gradle.kts`
- Adicionar dependências para o `DataStore` (Preferences) e `ViewModel` para o Jetpack Compose.

### Infraestrutura base e DataStore
#### [NEW] `com.example.droidmentor.preferences.PreferencesRepository.kt`
- Lógica para gerir a API Key com `DataStore` (ler e escrever a key).
#### [NEW] `com.example.droidmentor.DependencyContainer.kt`
- Interface do nosso Service Locator manual, baseada nos exemplos das aulas.
#### [NEW] `com.example.droidmentor.DroidMentorApplication.kt`
- Implementação do `Application` e inicialização do container.
#### [MODIFY] `app/src/main/AndroidManifest.xml`
- Registar a nova classe `DroidMentorApplication`.

### Ecrãs UI (Membro 1)
#### [NEW] `com.example.droidmentor.ui.settings.SettingsScreen.kt`
- UI para o utilizador introduzir e guardar a API Key.
#### [NEW] `com.example.droidmentor.ui.settings.SettingsViewModel.kt`
- ViewModel para gerir o estado da API Key.
#### [NEW] `com.example.droidmentor.ui.about.AboutScreen.kt`
- Ecrã estático com as informações da app e autores.

#### [MODIFY] `com.example.droidmentor.MainActivity.kt`
- Configurar uma navegação básica inicial só para testar estes 2 ecrãs.

## User Review Required
> [!NOTE]
> Lê o plano e aprova se a estrutura e tarefas corresponderem ao que idealizaste. Assim que aprovares clicando no botão "Proceed", avanço para a codificação automática das dependências e da arquitetura base (DataStore + UI de Settings/About).