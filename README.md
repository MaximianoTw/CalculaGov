# 🇧🇷 CalculaGov - Seu Contador Digital IRPF 2026

O **CalculaGov** é um assistente inteligente para cálculos de Imposto de Renda Pessoa Física (IRPF), desenvolvido com a identidade visual oficial do portal **gov.br**. Ele transforma a complexidade do leão em um processo simples, rápido e seguro.

---

## 🚀 Funcionalidades Principais

- **Visual Oficial gov.br:** Interface familiar e confiável, utilizando os padrões de design do Governo Federal.
- **Perfil Inteligente:** Salve seus dados (Nome, Renda, Dependentes) uma única vez e preencha simuladores automaticamente.
- **Cálculo Profissional:** Algoritmo atualizado com a tabela de IRPF 2026, considerando todas as deduções legais.
- **Histórico de Simulações:** Acompanhe a evolução dos seus cálculos com cards coloridos (Verde para restituição, Vermelho para imposto).
- **Relatório em PDF:** Gere um recibo digital detalhado com um clique para conferência posterior.
- **Privacidade Local:** Seus dados financeiros são sensíveis e, por isso, ficam armazenados **apenas no seu celular**.

---

## 📸 Guia Visual e Operação

### 1. Cadastro e Login
Para começar, crie sua conta informando seu nome e dados básicos. O app utiliza seu CPF como identificador único para salvar suas preferências.
> **Dica:** Preencha a renda bruta e o número de dependentes logo no cadastro para economizar tempo depois!

*(Placeholder para imagem da tela de Login/Cadastro)*

### 2. Home (Área do Cidadão)
A tela inicial é personalizada com seu nome e mostra um resumo do seu último cálculo realizado, para que você tenha a informação que importa sempre à mão.
*   **Alerta de Perfil:** Se esquecer de preencher algum dado importante, o app avisará você aqui.

*(Placeholder para imagem da Home Personalizada)*

### 3. Simulador de Imposto de Renda
Nesta tela, você tem duas opções:
- **Modo Automático:** Clique em "Preencher com meu perfil" e o app carrega seus dados instantaneamente.
- **Modo Manual:** Digite rendas extras, gastos com saúde ou educação conforme seus recibos.

*(Placeholder para imagem do Formulário de Cálculo)*

### 4. Resultado Detalhado e Exportação
Após o cálculo, você verá o parecer do "Contador Digital". Se o resultado for **Restituição**, o card ficará em destaque. 
*   **Gerar Recibo:** Use o botão de PDF para salvar o resumo detalhado na pasta de Documentos do seu celular.

*(Placeholder para imagem do Resultado e PDF)*

### 5. Histórico e Gestão de Perfil
- **Histórico:** Navegue por todas as simulações já feitas.
- **Perfil:** Atualize sua renda ou dependentes sempre que houver mudanças na sua vida financeira.

*(Placeholder para imagens das abas Histórico e Perfil)*

---

## 🛠️ Como Instalar e Rodar

1.  **Clone o projeto:** `git clone https://github.com/MaximianoTw/CalculaGov.git`
2.  **Abra no Android Studio:** Certifique-se de ter o SDK 34 (Android 14) instalado.
3.  **Dependências:** O app utiliza `iText7` para PDFs e `Material Design 3`. O Gradle sincronizará tudo automaticamente.
4.  **Execução:** Rode em um emulador ou dispositivo físico com Android 7.0 (API 24) ou superior.

---

## 🛡️ Segurança de Dados
Este aplicativo **não envia dados para servidores externos**. Todas as informações (Nome, CPF, Renda, Gastos) são gravadas via `SharedPreferences` de forma privada, acessíveis apenas pelo próprio aplicativo no dispositivo do usuário.

---
*Aviso: Este app é um simulador pedagógico e não possui vínculo oficial com a Receita Federal do Brasil.*
