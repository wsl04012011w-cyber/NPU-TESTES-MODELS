# QAIRT / QNN integration notes

## SDK and licensing

The Qualcomm AI Engine Direct SDK (QAIRT/QNN) is distributed by Qualcomm under its own product license. Obtain it from the official [Qualcomm AI Engine Direct SDK page](https://www.qualcomm.com/developer/software/qualcomm-ai-engine-direct-sdk) and review/accept the license for the version you use.

Do not mirror proprietary SDK archives, headers, libraries, Hexagon skeletons, or firmware components in this public repository. Do not download the SDK from unofficial mirrors. The repository therefore contains no QNN binaries.

## Android runtime packaging

The future Android HTP runtime integration will need a mutually compatible set of arm64 Android QNN libraries and HTP companion libraries from the same QAIRT release. The exact set depends on SDK version and Hexagon target. Do not mix versions or rename QNN libraries.

The optional local packaging location is:
`app/src/main/jniLibs/arm64-v8a/`

Only place files there if their license permits redistribution inside this app. For local/private testing, keep proprietary SDK content out of Git and use a private artifact source or local build input.

## Current app diagnostics

- Vulkan probe creates a Vulkan instance, enumerates physical devices, finds a compute-capable queue family, and attempts to create a logical device and retrieve the queue.
- This verifies that the app can access a Vulkan compute queue. It is not yet a shader execution or throughput benchmark.
- QNN probe attempts to load `libQnnHtp.so`, `libQnnSystem.so`, and `libQnnHtpPrepare.so`.
- A successful `dlopen` only means the library is loadable. It does **not** prove that HTP initialized or that a graph ran on the NPU.
- A failed `dlopen` may mean the library is not packaged, a dependency is missing, or Android's linker namespace prevents access. It does not prove that the phone lacks an NPU.

## Next HTP milestone

1. Confirm the exact QAIRT release and supported Hexagon target for the Galaxy A36 firmware.
2. Package the matching licensed arm64 Android runtime libraries through a permitted private distribution route.
3. Use QNN public headers from that same SDK to call backend/device APIs and enumerate HTP.
4. Load a small precompiled QNN graph, execute it, validate output, and record QNN profiling/backend evidence.
5. Report requested backend, initialized backend, graph execution backend, latency, memory, and fallback status separately.

References:
- [Qualcomm AI Engine Direct SDK](https://www.qualcomm.com/developer/software/qualcomm-ai-engine-direct-sdk)
- [Qualcomm QAIRT HTP documentation](https://docs.qualcomm.com/bundle/publicresource/topics/80-63442-10/htp_backend.html)
- [Android Vulkan compute migration guide](https://developer.android.com/guide/topics/renderscript/migrate/migrate-vulkan)
