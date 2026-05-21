# 🇧🇷 CalculaGov - Planejador Fiscal e Organizador de Evidências
> **Projeto de Extensão Universitária – Produção de Tecnologia Social**

O **CalculaGov** é um ecossistema digital mobile desenvolvido para democratizar o acesso à gestão fiscal no Brasil. Mais que um simulador, o aplicativo atua como um assistente de cidadania, integrando Inteligência Artificial, segurança biométrica e serviços públicos digitais para simplificar a jornada do IRPF 2026.

---

## 1. DIAGNÓSTICO E TEORIZAÇÃO

### 1.1 - Identificação das partes envolvidas e parceiros
Este projeto foi concebido para impactar cidadãos de baixa e média renda da comunidade local, trabalhadores assalariados e autônomos com renda mensal entre 1 e 5 salários mínimos. 
- **Desenvolvedor:** Aluno do Curso de [Seu Curso], Faculdade [Nome da Instituição].
- **Público-alvo:** Residentes de comunidades locais com dificuldade em organizar recibos e entender a legislação tributária.

### 1.2 - Situação-problema identificada
A ausência de ferramentas gratuitas e acessíveis resulta em desorganização financeira (perda de recibos dedutíveis), gastos desnecessários com consultorias básicas e insegurança digital, levando muitos cidadãos a caírem na "Malha Fina" por erros de preenchimento que poderiam ser evitados com conferência prévia.

### 1.3 - Demanda sociocomunitária e motivação acadêmica
O projeto atende à necessidade de transformar o celular em um "contador de bolso". Academicamente, a motivação reside na aplicação prática de **Visão Computacional (ML Kit)**, **Segurança Mobile (Biometria)** e **Integração de APIs de Governo (Brasil API)**, consolidando conhecimentos de engenharia de software voltados ao impacto social.

### 1.4 - Objetivos a serem alcançados
- Realizar simulações precisas de IRPF (Mensal e Anual) baseadas na tabela oficial.
- Automatizar a coleta de dados de recibos via OCR (Scanner de IA).
- Garantir 100% de privacidade através do isolamento de dados por CPF e biometria opcional.
- Gerar dossiês em PDF com as fotos das evidências anexadas.

---

## 2. PLANEJAMENTO E METODOLOGIA

### 2.1 - Plano de trabalho e Cronograma
1. **Semana 1-2:** Levantamento de requisitos e estudo da legislação tributária 2026.
2. **Semana 3-4:** Modelagem UI/UX seguindo o padrão de design oficial **gov.br**.
3. **Semana 5-8:** Desenvolvimento core: cálculos, integração Brasil API e ML Kit.
4. **Semana 9-10:** Implementação de camadas de segurança e isolamento multi-usuário.
5. **Semana 11-12:** Testes de estresse, validação de precisão e auditoria de privacidade.

### 2.2 - Metodologia (Tech Stack)
- **Linguagem:** Java (Android SDK).
- **Rede:** Retrofit + Gson (Integração com Brasil API para busca de CEP).
- **IA:** Google ML Kit (Extração de valores monetários de imagens).
- **Segurança:** `androidx.biometric` e `SharedPreferences` isoladas por hash de CPF.
- **Relatórios:** iText7 para exportação de PDFs de auditoria.

### 2.3 - Avaliação dos resultados
O sucesso do projeto foi medido pela precisão matemática dos cálculos (comparados ao sistema da Receita Federal) e pela eficácia da separação de diretórios, garantindo que um usuário nunca acesse os documentos de outro no mesmo dispositivo.

---

## 3. FUNCIONALIDADES DE IMPACTO

### 🔐 Segurança e Privacidade Multi-usuário
- **Isolamento Total:** Históricos de cálculos e pastas de fotos são criados dinamicamente usando o CPF como chave única.
- **Biometria Flexível:** O cidadão pode optar por exigir a digital para abrir áreas sensíveis (Cálculos, Histórico, Perfil) através de um switch nas configurações de Perfil.
- **Backup Desativado:** Configuração de segurança que impede a extração de dados sensíveis via backups externos (ADB).

### 🤖 IA Auditora (OCR)
O módulo de simulação permite fotografar um recibo. A IA identifica o valor mais relevante e preenche o campo automaticamente, reduzindo erros humanos de digitação.

### 🗺️ Integração com Brasil API
No cadastro de perfil, ao digitar os 8 dígitos do CEP, o app consome a **Brasil API** para preencher Cidade, UF e Logradouro instantaneamente, melhorando a experiência do usuário.

### 📅 Painel dinâmico (Home)
- **Calendário Fiscal:** Exibição do prazo de entrega das declarações.
- **Dicas do Leão:** Conteúdo educativo rotativo para prevenir erros fiscais.

---

## 4. EVIDÊNCIAS DE DESENVOLVIMENTO

O desenvolvimento foi registrado através de:
1. **Capturas de IDE:** Mostrando a implementação do `BiometricHelper` e lógica de cálculo.
2. **Auditoria de Arquivos:** Prova técnica da criação de subpastas `/Pictures/{CPF}` para isolamento de dados.
3. **Simulação de Fluxo:** Gravações do scanner de IA extraindo dados de recibos reais.
4. **Layout Responsivo:** Validação do aplicativo em diferentes resoluções (celulares e tablets) sem distorção.

---

## 🛠️ Como Executar
1. Clone o repositório.
2. Abra no Android Studio (Hedgehog ou superior).
3. Realize o **Gradle Sync**.
4. Execute em um emulador ou dispositivo físico com Android 7.0 (API 24) ou superior.

---
*Este aplicativo é uma ferramenta de simulação pedagógica. Os dados finais devem sempre ser validados no programa oficial da Receita Federal.*
