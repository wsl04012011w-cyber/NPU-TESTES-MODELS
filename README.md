# Snapdragon AI Lab

Laboratório Android para investigar inferência local e comparar CPU, GPU Adreno e NPU Hexagon em dispositivos Qualcomm.

> **Status:** protótipo inicial. Já existe uma primeira tela Android com Device Inspector e benchmark CPU. GPU Vulkan e QNN/HTP ainda não estão integrados nem validados.

## Dispositivo-alvo inicial

- **Aparelho:** Samsung Galaxy A36 5G
- **SoC:** Qualcomm Snapdragon 6 Gen 3
- **Memória física:** 6 GB
- **Memória livre observada pelo usuário:** aproximadamente 2,8 GB em uso normal (valor dinâmico; não representa memória garantida ao processo)
- **GPU:** Adreno 710
- **NPU:** Qualcomm Hexagon; acesso efetivo via HTP precisa ser validado no aparelho.

## O que já está no código

- Projeto Android nativo com Kotlin e Jetpack Compose.
- Tela **Device Inspector** com fabricante, modelo, Android/API, hardware, board, ABIs e memória disponível.
- Sinalizador inicial de disponibilidade da feature Vulkan (não é ainda um teste de compute).
- Benchmark CPU determinístico de contagem de primos.
- A interface deixa explícito que GPU e NPU ainda não foram testadas: não há falsa indicação de aceleração.

## Abrir o projeto

1. Baixe/clonar este repositório.
2. Abra a pasta raiz no Android Studio.
3. Aguarde a sincronização do Gradle.
4. Conecte o Galaxy A36 com depuração USB habilitada e execute o app.

O projeto ainda não inclui Gradle Wrapper nem um APK pré-compilado. A compilação precisa ser feita no Android Studio com JDK/SDK Android compatíveis.

## Próximas etapas

1. Confirmar versão Android/One UI e coletar dados reais do Device Inspector.
2. Implementar um teste independente de Vulkan compute para a Adreno.
3. Investigar e validar inicialização QNN/HTP no firmware do Snapdragon 6 Gen 3.
4. Criar uma camada de registro do backend solicitado **e** do backend confirmado pelo runtime.
5. Integrar modelos pequenos e quantizados; depois avançar para texto, visão, áudio e geração de imagens.

## Cuidado com o HTP no Galaxy A36

A lista pública de dispositivos/toolchains QAIRT consultada não identificava explicitamente o Snapdragon 6 Gen 3. Há também um relato público de falha de inicialização do transporte HTP num Galaxy A36 com Snapdragon 6 Gen 3. Isso não prova que a NPU seja inacessível em todos os firmwares e runtimes. O relato do usuário de geração de imagens acelerada é uma pista importante, mas precisamos identificar e registrar o backend efetivo.

## Arquitetura planejada

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

## Regras de benchmark

- Registrar modelo, versão/quantização, entrada e parâmetros.
- Separar tempo de carregamento de tempo de inferência.
- Aquecer o runtime e repetir execuções; reportar mediana.
- Registrar memória e estado térmico quando disponível.
- Distinguir backend solicitado de backend confirmado.
- Nunca afirmar NPU/GPU se ocorreu fallback para CPU.

## Referências

- [Qualcomm AI Runtime SDK](https://docs.qualcomm.com/bundle/publicresource/topics/80-63442-10/general_overview.html)
- [Qualcomm AI Hub Apps](https://github.com/qualcomm/ai-hub-apps)
- [Local Dream](https://github.com/xororz/local-dream)
- [ToolNeuron](https://github.com/Siddhesh2377/ToolNeuron)
- [Relato de HTP no Galaxy A36](https://github.com/qualcomm/ai-hub-apps/issues/130)
