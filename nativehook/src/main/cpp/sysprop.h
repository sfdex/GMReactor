#include "iostream"

namespace SysHook {
    class Model {
    private:
        std::string brand;
        std::string manufacturer;
        std::string model;
        std::string device;
        std::string board;
        std::string product;
        std::string specificPkg;
        std::string fullModelName;

    public:
        void init();

        bool isXiaomi();

        std::string get(const std::string &key);
    };

    void init();

    void hook_read_callback();
}