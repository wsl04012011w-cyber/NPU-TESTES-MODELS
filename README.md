# Snapdragon AI Lab

Laboratório Android para testar inferência local e comparar CPU, GPU Adreno e NPU Hexagon em dispositivos Qualcomm.

> **Status:** repositório inicial. Ainda não há código de aplicativo nem modelos incluídos.

## Objetivos

- Detectar hardware, APIs gráficas, memória e recursos disponíveis no aparelho.
- Executar modelos localmente, sem depender de serviços na nuvem.
- Comparar CPU, GPU e NPU com o mesmo modelo e entradas equivalentes sempre que possível.
- Mostrar o backend realmente selecionado, latência, memória, temperatura/estado térmico quando disponível e consumo estimado quando mensurável.
- Permitir tarefas variadas: texto, visão, áudio e, quando houver modelo/runtime compatível, geração de imagens.

## Arquitetura proposta

```text
Android UI (Kotlin + Jetpack Compose)
              |
       AI Lab Controller
              |
    Backend / Capability Registry
       /          |          \
    CPU          GPU         NPU
  CPU runtime  GPU runtime  Qualcomm QAIRT / QNN HTP
       \          |          /
          Benchmark Runner
              |
      Resultados e logs
```

A seleção do backend será explícita. Não vamos afirmar que houve aceleração por GPU/NPU apenas porque a inferência terminou: cada execução deve registrar o backend solicitado, o backend confirmado pelo runtime e eventuais fallbacks.

## Estratégia de implementação

1. **Device Inspector:** identificar SoC, ABI, versão Android, memória, GPU e APIs disponíveis.
2. **GPU Lab:** validar primeiro um workload de compute simples e depois inferência por um runtime compatível.
3. **NPU Lab:** validar o backend Qualcomm HTP com um modelo pequeno e quantizado, registrando logs/profiling.
4. **CPU Baseline:** executar o mesmo workload na CPU para comparação.
5. **Model Lab:** adicionar tarefas de texto, visão, áudio e geração de imagem conforme suporte real do aparelho e dos modelos.
6. **Hybrid Experiments:** testar divisão de operações entre backends apenas quando o runtime/modelo suportar isso; evitar cópias de tensores que eliminem o ganho.

## Stack inicial

- Android nativo: Kotlin + Jetpack Compose.
- Camada nativa opcional: C++/JNI para integrações que exigirem API nativa.
- NPU Qualcomm: Qualcomm AI Runtime SDK (QAIRT), QNN e backend HTP, sujeito à compatibilidade do SoC, firmware e SDK.
- GPU: começar com Vulkan compute para validar acesso à Adreno; depois testar backend de inferência GPU suportado pelo runtime.
- CPU: baseline de referência.

As dependências e versões do SDK serão fixadas depois de identificar o modelo exato do aparelho, versão Android e suporte real dos runtimes. O Snapdragon 6 Gen 3 anuncia CPU Kryo, GPU Adreno com Vulkan 1.1/OpenCL 2.0 e Hexagon; isso não garante, por si só, que qualquer versão do QAIRT ou qualquer modelo seja compatível.

## Regras de benchmark

- Registrar modelo, versão/quantização, resolução/tamanho da entrada e parâmetros.
- Aquecer o runtime antes de medir.
- Repetir execuções e apresentar mediana, não só o melhor tempo.
- Registrar latência de carregamento separada da latência de inferência.
- Observar memória e throttling térmico.
- Distinguir backend solicitado de backend efetivamente utilizado.
- Não comparar resultados com entradas, precisões ou configurações diferentes sem deixar isso explícito.

## Próximo passo

Implementar o **Device Inspector** e um teste de GPU Vulkan independente do runtime de IA. Em paralelo, identificar o aparelho e verificar a disponibilidade do backend HTP/QAIRT nele.

## Referências oficiais

- [Snapdragon 6 Gen 3](https://www.qualcomm.com/smartphones/products/6-series/snapdragon-6-gen-3-mobile-platform)
- [Qualcomm AI Runtime SDK](https://docs.qualcomm.com/bundle/publicresource/topics/80-70020-15B/qairt.html)
- [Qualcomm AI Hub Apps](https://github.com/qualcomm/ai-hub-apps)
- [Qualcomm AI Hub Models](https://github.com/qualcomm/ai-hub-models)
