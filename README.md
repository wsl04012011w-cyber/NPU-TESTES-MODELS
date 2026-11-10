# Snapdragon AI Lab

Laboratório Android para investigar inferência local e comparar CPU, GPU Adreno e NPU Hexagon em dispositivos Qualcomm.

> **Status:** protótipo 0.2.0. Inclui Device Inspector, benchmark CPU, módulo nativo NDK para diagnóstico Vulkan e verificação de carregamento das bibliotecas QNN.

## Dispositivo-alvo inicial

- **Aparelho:** Samsung Galaxy A36 5G
- **SoC:** Qualcomm Snapdragon 6 Gen 3
- **Memória física:** 6 GB
- **Memória livre observada pelo usuário:** aproximadamente 2,8 GB em uso normal (valor dinâmico; não representa memória garantida ao processo)
- **GPU:** Adreno 710
- **NPU:** Qualcomm Hexagon; acesso efetivo via QNN/HTP ainda precisa ser validado no firmware.

## O que já está no código

- Android nativo com Kotlin, Jetpack Compose, C++ e JNI.
- Device Inspector com fabricante, modelo, Android/API, hardware, board, ABIs e RAM.
- Benchmark CPU determinístico de contagem de primos.
- **GPU/Vulkan:** cria uma instância Vulkan, enumera dispositivos físicos, procura fila COMPUTE e tenta criar dispositivo lógico/obter a fila.
- **QNN/HTP:** tenta carregar as bibliotecas centrais `libQnnHtp.so`, `libQnnSystem.so` e `libQnnHtpPrepare.so`.
- Build arm64-v8a com Android NDK e CMake via GitHub Actions.

### Limites importantes dos diagnósticos

O teste Vulkan atual confirma acesso ao dispositivo e à fila de compute, mas ainda não executa shader nem mede throughput.

O teste QNN atual verifica apenas se bibliotecas podem ser carregadas. Isso **não** confirma inicialização HTP, enumeração da NPU ou execução de inferência. A confirmação exige APIs QNN, runtime compatível, driver e execução validada de um grafo.

## Build sem computador

O workflow `.github/workflows/android-apk.yml` compila o APK arm64-v8a no GitHub Actions. Abra a aba Actions e baixe o artefato `Snapdragon-AI-Lab-debug-arm64` de uma execução concluída.

## QAIRT / QNN SDK

O Qualcomm AI Engine Direct SDK (QAIRT/QNN) tem licença própria da Qualcomm e pode exigir login/aceitação de licença. **Não incluímos binários ou headers proprietários no repositório público.** Consulte [docs/QAIRT_SETUP.md](docs/QAIRT_SETUP.md) e obtenha o SDK pela [página oficial da Qualcomm](https://www.qualcomm.com/developer/software/qualcomm-ai-engine-direct-sdk).

Não misture versões de bibliotecas QNN, HTP stub e skel. Só empacote componentes quando a licença permitir.

## Próximas etapas

1. Instalar e executar o APK no Galaxy A36; coletar o relatório Vulkan e QNN.
2. Adicionar execução real de shader Vulkan e benchmark de compute.
3. Integrar APIs QNN licenciadas e validar enumeração/inicialização HTP.
4. Executar um grafo pequeno, validar saída e registrar prova do backend efetivo.
5. Adicionar modelos de visão, texto, áudio e geração de imagens.

## Regras de benchmark

- Separar tempo de carregamento e tempo de inferência.
- Aquecer o runtime e repetir execuções; reportar mediana.
- Registrar memória e estado térmico quando disponível.
- Distinguir backend solicitado, inicializado e realmente usado.
- Nunca afirmar NPU/GPU se ocorreu fallback para CPU.

## Referências

- [Qualcomm AI Engine Direct SDK](https://www.qualcomm.com/developer/software/qualcomm-ai-engine-direct-sdk)
- [Qualcomm QAIRT HTP documentation](https://docs.qualcomm.com/bundle/publicresource/topics/80-63442-10/htp_backend.html)
- [Android Vulkan compute guide](https://developer.android.com/guide/topics/renderscript/migrate/migrate-vulkan)
- [Local Dream](https://github.com/xororz/local-dream)
- [ToolNeuron](https://github.com/Siddhesh2377/ToolNeuron)
