# Plano de Projeto: DroidMentor

Este documento detalha a divisão de tarefas e o planeamento das entregas (milestones) para a aplicação **DroidMentor**, um cliente LLM BYOK focado em mentoria Android. O trabalho está dividido de forma equilibrada entre os 3 elementos do grupo, focando em "fatias verticais" organizadas por ecrãs e infraestrutura base.

## Equipa e Papéis

### Membro 1: Infraestrutura, Tema Visual e Ecrãs de Suporte
Responsável pela fundação arquitetónica da app, segurança de dados, sistema de design global e gestão de estado da rede (offline-first).
*   **UI:** Ecrãs *Settings* e *About*. Criação do *Design System* (tema, cores, tipografia) e componentes globais de feedback (ex: *Snackbars* ou alertas para estado offline e erros HTTP). Estrutura de navegação principal.
*   **Dados:** Implementação do `DataStore` para guardar a chave da API (BYOK) em segurança.
*   **Arquitetura:** Criação da classe `Application` configurada como *Service Locator* para injeção de dependências (sem usar Hilt/Dagger).

### Membro 2: Gestão de Sessões, Navegação e Listas Dinâmicas
Responsável pela entrada na aplicação, gestão visual do histórico de conversas e encaminhamento inteligente.
*   **UI:** Ecrãs *Title* e *Chat History*. Criação das listas dinâmicas para apresentar as conversas, estados vazios (*Empty States*) e interface para remoção de conversas.
*   **Dados:** Configuração inicial da base de dados `Room` e das entidades de domínio para armazenar o histórico de chats.
*   **Lógica:** Implementar o redirecionamento automático do *Title* para o *Active Chat* se a sessão anterior não tiver sido devidamente fechada.

### Membro 3: Interface de Conversação e Integração LLM
Responsável pelo motor principal da aplicação, comunicação com a API do Gemini e interações complexas no ecrã de chat.
*   **UI:** Ecrã *Active Chat* (balões de conversa, campo de introdução de texto, animações de carregamento). UI para reescrita de mensagens (edição) e anexação de imagens (requisito opcional).
*   **Rede:** Configuração do cliente `Ktor` e `Kotlinx Serialization`. Implementação da lógica *stateless* (construir e enviar o histórico completo a cada pedido) e configuração do `system_instruction` (persona do mentor).
*   **Dados (Avançado):** Lógica para invalidar histórico após a reescrita de uma mensagem. No requisito opcional, gravar imagens localmente e persistir a URI no `Room` (sem converter para Base64).

---

## Planeamento de Entregas (Milestones)


### Milestone 2 (12/10/2026) - Fundações e UI Base
*   **Membro 1:** Desenvolver UI estática de *Settings* e *About*. Implementar o `DataStore` para gravar a API Key.
*   **Membro 2:** Criar UI dos ecrãs *Title* e *Chat History*. Configurar a base de dados `Room` com as tabelas iniciais.
*   **Membro 3:** Desenvolver a UI base do *Active Chat*. Configurar o cliente `Ktor` e realizar a primeira chamada de teste ao endpoint `generateContent`.
*   **Grupo:** Gravar vídeo demo (5-7 min) discutindo decisões e tag `mentor_2`.

### Milestone 3 (16/11/2026) - Lógica Core e Resiliência
*   **Membro 1:** Implementar a lógica *Offline-first* (verificação de rede) e os componentes visuais para tratamento de erros HTTP (429, 500).
*   **Membro 2:** Implementar a funcionalidade de remover conversas anteriores (Room + UI). Adicionar lógica de redirecionamento automático no arranque da app.
*   **Membro 3:** Implementar a lógica *stateless* no pedido Ktor (enviar o histórico completo) e configurar a *persona*. Ligar as mensagens em tempo real à base de dados `Room`.
*   **Grupo:** Gravar vídeo demo (5-7 min) demonstrando o fluxo completo de rede e tag `mentor_3`.

### Final Milestone (12/12/2026) - Funcionalidades Avançadas e Polimento
*   **Membro 1:** Polimento final do *Design System*, navegação e responsividade geral da interface.
*   **Membro 2:** Garantir a robustez das transições e estados de lista vazia no histórico de sessões.
*   **Membro 3:** Implementar a edição/reescrita de mensagens antigas. Implementar requisito opcional (seleção de imagens, gravação local, envio via Ktor).
*   **Grupo:** Testes exaustivos, gravação do vídeo demo final (5-7 min) e tag `mentor_f`.
