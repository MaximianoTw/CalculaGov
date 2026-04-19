# 🇧🇷 CalculaGov - Seu Contador Digital IRPF 2026

O **CalculaGov** evoluiu de um simples simulador para um **Ecosistema Digital de Gestão Fiscal**. Com identidade visual oficial do padrão **gov.br**, o app agora atua como um assistente completo para o IRPF 2026, garantindo segurança, organização de documentos e economia real.

---

## 🚀 Novidades da Versão "Contador Digital"

### 🔐 Segurança Biométrica e Privacidade
- **Login Seguro:** Acesso protegido por **Biometria (Digital/Face)** via `BiometricHelper`, integrado ao padrão de segurança gov.br.
- **Privacidade Máxima:** Logout rápido (`btnPowerOff`) e máscaras de dados (CPF/RG) que protegem sua identidade visualmente.
- **Deep Linking:** Preparado para integração com o app oficial `br.gov.meugovbr`.

### 🤖 IA Auditora e Gestão de Evidências (OCR)
- **Câmera Inteligente:** Use a câmera para ler recibos médicos ou escolares. O **Google ML Kit** identifica valores e preenche os campos automaticamente.
- **Pasta de Comprovantes:** Galeria dedicada para organizar seus recibos. Agora com funcionalidade de **exclusão individual** para manter sua nuvem limpa.
- **Proteção Malha Fina:** Gere relatórios PDF que anexam as fotos dos comprovantes, servindo como sua pasta de evidências digital.

### 📉 Comparativo de Modelos e Automação
- **Consultoria Automática:** Cálculo simultâneo entre os modelos **Simplificado** e **Completo**, com alerta visual (Verde/Azul) da melhor opção.
- **Histórico Inteligente:** Salve suas simulações e gerencie-as individualmente (exclua o que não serve mais).
- **Auto-preenchimento:** Importe dados de renda e dependentes diretamente do seu perfil verificado.

### 📅 Painel do Cidadão (Home)
- **Calendário Fiscal 2026:** Acompanhe os prazos de entrega (15 de Março a 31 de Maio) com interface dinâmica.
- **Dicas do Leão:** Carrossel educativo com orientações sobre deduções e boas práticas fiscais.

---

## 📸 Funcionalidades em Destaque

### 1. Central de Comprovantes
Gerencie todos os seus recibos lidos pela IA em um só lugar. Use o ícone de lixeira (`ic_delete`) para remover documentos antigos com feedback instantâneo.

### 2. Relatório de Auditoria (PDF)
Gere um PDF profissional via **iText7** que inclui não apenas os cálculos, mas também as **fotos dos recibos**. Graças ao novo `FileProvider`, o compartilhamento e visualização são imediatos e seguros.

### 3. Máscaras e Validação
Interface otimizada para entrada de dados brasileira (CPF: `000.000.000-00`), evitando erros de preenchimento que levam à malha fina.

---

## 🛠️ Tecnologias e APIs

- **Segurança:** `androidx.biometric` para autenticação nativa.
- **Inteligência Artificial:** `ML Kit Text Recognition` para OCR de recibos.
- **Relatórios:** `iText7` para geração de documentos fiscais complexos.
- **Persistência:** `SharedPreferences` para histórico de cálculos com gestão de estado.
- **UI/UX:** Material Design 3 com a paleta oficial `#00387E` (Azul) e `#FFCC00` (Ouro).

---

## 💻 Como Operar

1.  **Acesse com Segurança:** Use sua biometria para entrar no perfil.
2.  **Organize-se:** Use a câmera para escanear recibos ao longo do ano.
3.  **Simule:** Use o "Auto-preencher" para carregar seus dados básicos.
4.  **Compare:** Siga a "Dica de Economia" para escolher o melhor modelo de declaração.
5.  **Exporte:** Gere seu dossiê em PDF com todas as evidências anexadas.

---
*Aviso: O CalculaGov é uma ferramenta de apoio e simulação. Sempre confira os dados finais no programa oficial da Receita Federal do Brasil.*
