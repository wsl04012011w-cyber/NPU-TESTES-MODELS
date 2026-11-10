# Snapdragon AI Lab

Laboratório Android para testar inferência local e comparar CPU, GPU Adreno e NPU Hexagon em dispositivos Qualcomm.

> **Status:** projeto em fase de protótipo. O repositório contém o plano inicial; a implementação Android será adicionada em seguida.

## Dispositivo-alvo inicial

- **Aparelho:** Samsung Galaxy A36 5G
- **SoC:** Qualcomm Snapdragon 6 Gen 3
- **Memória física:** 6 GB
- **Memória livre observada pelo usuário:** aproximadamente 2,8 GB em uso normal (valor dinâmico; não representa memória garantida ao processo)
- **GPU:** Adreno 710
- **GPU API anunciada:** Vulkan 1.1
- **NPU:** Qualcomm Hexagon; o acesso efetivo via HTP ainda precisa ser validado no aparelho.

### Atenção especial ao HTP neste aparelho

A documentação pública atual do QAIRT lista diversos SoCs e toolchains, mas a lista consultada não identifica explicitamente o Snapdragon 6 Gen 3. Também existe um relato público de um Galaxy A36 5G com Snapdragon 6 Gen 3 em que a inicialização do transporte HTP falhou usando QNN/TFLite delegate. Isso **não prova** que a NPU seja inacessível em todas as versões de firmware/runtime; significa que devemos tratar o HTP como uma hipótese a validar, não como recurso garantido.

Por isso, a primeira versão deve:
1. Inspecionar propriedades do dispositivo e APIs realmente expostas.
2. Executar um teste Vulkan compute independente para validar a GPU.
3. Tentar inicializar HTP com diagnóstico claro e guardar logs de falha/sucesso.
4. Manter CPU como baseline funcional.
5. Não apresentar uma execução como NPU se o runtime tiver feito fallback.

Com 6 GB de RAM, os testes iniciais devem usar modelos pequenos/quantizados e medições curtas. A memória livre varia conforme o sistema e outros aplicativos; evitar manter vários modelos carregados ao mesmo tempo.

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
       /          |          \\
    CPU          GPU         NPU
  CPU runtime  Vulkan /     Qualcomm QAIRT / QNN HTP
               GPU runtime
       \\          |          /
          Benchmark Runner
              |
      Resultados e logs
```

A seleção do backend será explícita. Não vamos afirmar que houve aceleração por GPU/NPU apenas porque a inferência terminou: cada execução deve registrar o backend solicitado, o backend confirmado pelo runtime e eventuais fallbacks.

## Estratégia de implementação

1. **Device Inspector:** identificar SoC, ABI, versão Android, memória, GPU e APIs disponíveis.
2. **GPU Lab:** validar primeiro um workload de compute simples via Vulkan e depois inferência por um runtime compatível.
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

As dependências e versões do SDK serão fixadas depois de identificar a versão Android e o suporte real dos runtimes no aparelho.

## Regras de benchmark

- Registrar modelo, versão/quantização, resolução/tamanho da entrada e parâmetros.
- Aquecer o runtime antes de medir.
- Repetir execuções e apresentar mediana, não só o melhor tempo.
- Registrar latência de carregamento separada da latência de inferência.
- Observar memória e throttling térmico.
- Distinguir backend solicitado de backend efetivamente utilizado.
- Não comparar resultados com entradas, precisões ou configurações diferentes sem deixar isso explícito.

## Próximo passo

Implementar o **Device Inspector** e um teste de GPU Vulkan independente do runtime de IA. Em paralelo, investigar a disponibilidade real do backend HTP/QAIRT neste firmware.

## Referências

- [Samsung Galaxy A36 5G — especificações](https://www.samsung.com/br/smartphones/galaxy-a/galaxy-a36-5g-awesome-white-128gb-sm-a366ezabzto/)
- [Qualcomm AI Runtime SDK — visão geral](https://docs.qualcomm.com/bundle/publicresource/topics/80-63442-10/general_overview.html)
- [Qualcomm QAIRT — dispositivos/toolchains suportados](https://docs.qualcomm.com/nav/home/QNN_general_overview.html?product=924033590759186372)
- [Qualcomm AI Hub Apps](https://github.com/qualcomm/ai-hub-apps)
- [Qualcomm AI Hub Models](https://github.com/qualcomm/ai-hub-models)
- [Relato de inicialização HTP no Galaxy A36 5G](https://github.com/qualcomm/ai-hub-apps/issues/130)
