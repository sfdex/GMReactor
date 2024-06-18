#include "log.h"
#include "fstream"
#include "sstream"
#include "bytehook.h"
#include "sys/system_properties.h"
#include "sysprop.h"

namespace SysHook {
    std::vector<std::string> split(const std::string &line, const char delimiter) {
        std::vector<std::string> pair;
        std::istringstream lineStream(line);
        std::string cell;
        while (std::getline(lineStream, cell, delimiter)) {
            pair.push_back(cell);
        }
        return pair;
    }

    void Model::init() {
        std::fstream inputFile("/data/local/tmp/genetically-modified-reactor.json");
        if (!inputFile) {
            LOGD("Cannot open file");
            return;
        }
        std::string line;
        while (std::getline(inputFile, line)) {
            LOGD("Model line: %s", line.c_str());
            const auto v = split(line, ':');
            const std::string &key = v[0];
            const std::string &value = v.size() > 1 ? v[1] : "";
            if (key == "brand") {
                this->brand = value;
            } else if (key == "manufacturer") {
                this->manufacturer = value;
            } else if (key == "model") {
                this->model = value;
            } else if (key == "device") {
                this->device = value;
            } else if (key == "board") {
                this->board = value;
            } else if (key == "product") {
                this->product = value;
            } else if (key == "specificPkg") {
                this->specificPkg = value;
            } else if (key == "fullModelName") {
                this->fullModelName = value;
            }
        }

        LOGD("Model init success: %s", this->model.c_str());
    }

    std::string Model::get(const std::string &key) {
        if (key == "ro.product.brand") {
            return brand;
        } else if (key == "ro.product.manufacturer") {
            return manufacturer;
        } else if (key == "ro.product.model") {
            return model;
        }
        return "";
    }

    bool Model::isXiaomi() {
        return strcasestr(this->brand.c_str(), "xiaomi");
    }

    Model model;

    void init() {
        model.init();
        bytehook_init(BYTEHOOK_MODE_AUTOMATIC, true);
        bytehook_set_recordable(true);
        bool is_debug = bytehook_get_debug();
        bool is_recordable = bytehook_get_recordable();
        const char *version = bytehook_get_version();
        LOGD("bytehook init result: version: %s, is_debug: %d, is_recordable: %d",
             version, is_debug, is_recordable);
    }

    struct prop_info {
        std::atomic_uint_least32_t serial;
        char value[PROP_VALUE_MAX];
        char name[0];
    };

    using callback_func = void(void *cookie, const char *name, const char *value, uint32_t serial);

    void proxy_system_property_read_callback(const prop_info *pi, callback_func *callbackFunc,
                                             void *cookie) {
        BYTEHOOK_STACK_SCOPE();
        LOGD("proxy_system_property_read_callback [%s]: [%s]", pi->name, pi->value);

        if (strstr(pi->name, "ro.product.model")
            || strstr(pi->name, "ro.product.brand")
            || strstr(pi->name, "ro.product.manufacturer")) {
            const auto value = model.get(pi->name);
            if (!value.empty()) {
                return callbackFunc(cookie, pi->name, value.c_str(), pi->serial);
            }
        }

        if (strcasestr(pi->name, "ro.mi.os.version")) {
            if (!model.isXiaomi()) {
                return callbackFunc(cookie, pi->name, "", pi->serial);
            }
        }

        return BYTEHOOK_CALL_PREV(proxy_system_property_read_callback, pi, callbackFunc, cookie);
    }

    bytehook_stub_t stub_read_callback = nullptr;

    void hook_read_callback() {
        const char *func_name = "__system_property_read_callback";
        stub_read_callback = bytehook_hook_all(
                nullptr,
                func_name,
                (void *) SysHook::proxy_system_property_read_callback,
                nullptr,
                nullptr);

        if (stub_read_callback != nullptr) {
            LOGD("hook func{%s} success", func_name);
        } else {
            LOGD("hook func{%s} failure", func_name);
        }
    }
}