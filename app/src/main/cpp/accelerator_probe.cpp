#include <jni.h>
#include <vulkan/vulkan.h>
#include <dlfcn.h>
#include <sstream>
#include <string>
#include <vector>

static std::string gpuProbe() {
    VkApplicationInfo app{VK_STRUCTURE_TYPE_APPLICATION_INFO};
    app.pApplicationName = "Snapdragon AI Lab";
    app.apiVersion = VK_API_VERSION_1_0;

    VkInstanceCreateInfo instanceInfo{VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO};
    instanceInfo.pApplicationInfo = &app;

    VkInstance instance = VK_NULL_HANDLE;
    VkResult result = vkCreateInstance(&instanceInfo, nullptr, &instance);
    if (result != VK_SUCCESS) {
        return "Vulkan instance: FAILED (VkResult " + std::to_string(result) + ")";
    }

    uint32_t count = 0;
    result = vkEnumeratePhysicalDevices(instance, &count, nullptr);
    if (result != VK_SUCCESS || count == 0) {
        vkDestroyInstance(instance, nullptr);
        return "Vulkan: no physical device enumerated (VkResult " + std::to_string(result) + ")";
    }

    std::vector<VkPhysicalDevice> devices(count);
    vkEnumeratePhysicalDevices(instance, &count, devices.data());
    std::ostringstream out;
    out << "Vulkan instance: OK\nPhysical devices: " << count << "\n";

    for (uint32_t i = 0; i < count; ++i) {
        VkPhysicalDeviceProperties props{};
        vkGetPhysicalDeviceProperties(devices[i], &props);
        uint32_t qCount = 0;
        vkGetPhysicalDeviceQueueFamilyProperties(devices[i], &qCount, nullptr);
        std::vector<VkQueueFamilyProperties> queues(qCount);
        vkGetPhysicalDeviceQueueFamilyProperties(devices[i], &qCount, queues.data());

        int computeFamily = -1;
        for (uint32_t q = 0; q < qCount; ++q) {
            if (queues[q].queueCount > 0 && (queues[q].queueFlags & VK_QUEUE_COMPUTE_BIT)) {
                computeFamily = static_cast<int>(q);
                if (!(queues[q].queueFlags & VK_QUEUE_GRAPHICS_BIT)) break;
            }
        }

        out << "Device " << i << ": " << props.deviceName
            << "\nAPI: " << VK_VERSION_MAJOR(props.apiVersion) << "."
            << VK_VERSION_MINOR(props.apiVersion)
            << "\nType: " << (props.deviceType == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU ? "Discrete GPU" :
                                props.deviceType == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU ? "Integrated GPU" :
                                props.deviceType == VK_PHYSICAL_DEVICE_TYPE_CPU ? "CPU/Software" : "Other")
            << "\nCompute queue: " << (computeFamily >= 0 ? "FOUND" : "NOT FOUND");

        if (computeFamily >= 0) {
            float priority = 1.0f;
            VkDeviceQueueCreateInfo queueInfo{VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO};
            queueInfo.queueFamilyIndex = static_cast<uint32_t>(computeFamily);
            queueInfo.queueCount = 1;
            queueInfo.pQueuePriorities = &priority;

            VkDeviceCreateInfo deviceInfo{VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO};
            deviceInfo.queueCreateInfoCount = 1;
            deviceInfo.pQueueCreateInfos = &queueInfo;

            VkDevice logicalDevice = VK_NULL_HANDLE;
            VkResult deviceResult = vkCreateDevice(devices[i], &deviceInfo, nullptr, &logicalDevice);
            if (deviceResult == VK_SUCCESS) {
                VkQueue queue = VK_NULL_HANDLE;
                vkGetDeviceQueue(logicalDevice, static_cast<uint32_t>(computeFamily), 0, &queue);
                out << "\nLogical device: CREATED"
                    << "\nCompute queue handle: " << (queue != VK_NULL_HANDLE ? "OK" : "NULL");
                vkDestroyDevice(logicalDevice, nullptr);
            } else {
                out << "\nLogical device: FAILED (VkResult " << deviceResult << ")";
            }
        }
        out << "\n";
    }

    vkDestroyInstance(instance, nullptr);
    out << "Note: this is a Vulkan device/queue validation, not yet a shader throughput benchmark.";
    return out.str();
}

static std::string qnnProbe() {
    const char* names[] = {"libQnnHtp.so", "libQnnSystem.so", "libQnnHtpPrepare.so"};
    std::ostringstream out;
    out << "QNN runtime library probe (does not initialize HTP):\n";
    for (const char* name : names) {
        dlerror();
        void* handle = dlopen(name, RTLD_NOW | RTLD_LOCAL);
        const char* error = dlerror();
        out << name << ": " << (handle ? "LOAD OK" : "NOT LOADABLE");
        if (!handle && error) out << " (" << error << ")";
        out << "\n";
        if (handle) dlclose(handle);
    }
    out << "HTP device initialization and inference require the licensed QAIRT SDK/runtime and a compatible device driver.";
    return out.str();
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_wsl_nputests_AcceleratorDiagnostics_nativeGpuProbe(JNIEnv* env, jobject) {
    const std::string result = gpuProbe();
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_wsl_nputests_AcceleratorDiagnostics_nativeQnnProbe(JNIEnv* env, jobject) {
    const std::string result = qnnProbe();
    return env->NewStringUTF(result.c_str());
}
