Optional QAIRT/QNN runtime libraries for arm64-v8a belong in this directory only when you have obtained them under Qualcomm's SDK license.
Do not commit Qualcomm SDK binaries or proprietary headers to this public repository.
Expected runtime libraries for a future HTP integration include libQnnHtp.so, libQnnSystem.so and version-matched companion libraries.
The current app only checks whether key libraries can be loaded; it does not initialize HTP or run a neural-network graph.
