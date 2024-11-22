### 进度安排

11.11~11.17：学习AFL与fuzz、使用afl-cc对target进行插装

11.17~11.24：实现种子排序、能量调度、变异、执行、监控组件

11.24~12.1：实现评估组件、进行封装与部署、记录使用视频

### 任务分配

成员A：顾秋炎221840039

成员B：郑陶

A：

1. 整理任务梳理指标
2. 搭建项目结构
3. 使用afl-cc对所有target进行插装
4. 实现种子排序、能量调度、执行、监控组件
5. 文档撰写

B：

1. 实现评估组件
2. 在target运行测试并记录数据
3. 打包与部署
4. 工具使用方法的演示
5. 文档撰写

### 开发日志

#### 插装

原理：在汇编语言的跳转点之后插入一个函数调用，记录程序运行时该跳转的执行情况

| TID  | Target    | Project                         | AFL-CMD            | Initial Seeds                                          |
| ---- | --------- | ------------------------------- | ------------------ | ------------------------------------------------------ |
| T01  | `cxxfilt` | `binutils-2.28.tar.gz`          | `cxxfilt`          | `"_Z1fv"`, (LLM-Generate)                              |
| T02  | `readelf` | `binutils-2.28.tar.gz`          | `readelf -a @@ @@` | `afl++/testcases/others/elf/`                          |
| T03  | `nm-new`  | `binutils-2.28.tar.gz`          | `nm-new @@`        | `afl++/testcases/others/elf/`                          |
| T04  | `objdump` | `binutils-2.28.tar.gz`          | `objdump -d @@`    | `afl++/testcases/others/elf/`                          |
| T05  | `djpeg`   | `libjpeg-turbo-3.0.4.tar.gz`    | `djpeg @@`         | `afl++/testcases/images/jpeg`, `<project>/testimages/` |
| T06  | `readpng` | `libpng-1.6.29.tar.gz`          | `readpng`          | `afl++/testcases/images/png/`, `<project>/tests/`      |
| T07  | `xmllint` | `libxml2-2.13.4.tar.gz`         | `xmllint @@`       | `afl++/testcases/others/xml/`, `<project>/test/`       |
| T08  | lua       | `lua-5.4.7.tar.gzd`             | `lua @@`           | https://github.com/lua/lua/tree/master/testes          |
| T09  | `mjs`     | `mjs-2.20.0.tar.gz`             | `mjs -f @@`        | `afl++/testcases/others/mjs/`, `<project>/tests/`      |
| T10  | `tcpdump` | `tcpdump-tcpdump-4.99.5.tar.gz` | `tcpdump -nr @@`   | `afl++/testcases/others/pcap/`, `<project>/tests/`     |

##### 对于makefile项目

```bash
CC=afl-cc ./configure
make
```

如果./configure文件不存在，那么就在makefile文件内修改

```bash
CC=afl-cc
```

检查是否成功插桩

```bash
afl-showmap -o /dev/null -- ./binutils/cxxfilt
```

然后就是找出对应的二进制文件，提取到项目文件夹中

```bash
find . -name cxxfilt
```

##### 对于cmake项目

```bash
export CC=afl-cc
export CXX=afl++-cxx  # 如果项目涉及 C++，需要指定 C++ 编译器
mkdir build
cd build
cmake ..
make djpeg 
```

后面的步骤就是一样的了

没找到target6对应的二进制可执行文件。

##### readpng

构建完毕后找不到readpng这个可执行文件，在makefile里查找也找不到有readpng这个构建目标。虽然说是找到了readpng.c和readpng.h，尝试用afl-cc进行编译时无法处理链接的一大堆文件，遂放弃。

##### mjs

将CC修改为afl-cc且构建完毕后，尝试检查插桩总会报以下错误，尝试多种方法后仍然失败。

![image-20241121120024066](https://typora-images-gqy.oss-cn-nanjing.aliyuncs.com/image-20241121120024066.png)

而关于initialSeed，在AFL++/testcases/others里也没有mjs文件夹，只有js文件夹。这个target应该是没法进行覆盖率统计等操作了。

#### 插件开发日志

##### first

最开始实现了一个原始的executor，传入一个seed和target的path，将target的输出打印到控制台上。

此时我想用afl-showmap的覆盖查询功能来实现一个seedAnalyser，再让seedSorter调用它，并将seedSorter插入到executor的执行逻辑之前。

但是这样一来同一个seed会被执行多次，而仅仅为了进行sort，后面的能量调度组件同样也需要调用seedAnalyser，这开销就太大了，而且都是在做重复的事情。

所以我决定添加Seed数据类。

##### Second

添加了Seed类之后，数据都存储在Seed类中，由executor负责获取数据，其他组件只需要调用Seed中的数据进行操作就好。

在这基础上，我添加了SeedSorter，EnergyScheduler，Logger类。并将executor中的execute逻辑实现为了类似bfs的两个queue轮转循环。

但当我想要进行Mutate的时候，问题又出现了：同一个Seed如果在执行后仍然被放入之前的种子队列，那么无疑它会被执行多次。

我需要一个独立的数据结构来存储已经执行完，只有变异价值的种子。

##### Third

因此就有了processedSeedList。每次执行完一遍，种子获得了执行数据，再进行sort和能量调度，这二者影响的是变异过程，只间接影响下一次的执行过程。

最后我做了Mutator的基础实现，再修订了一下execute逻辑中的IncrementalCoverage计算逻辑，这个程序就可以获得持续变异的种子，持续不断地运行，并提升覆盖度直到一个瓶颈了。

### 疑难杂症

#### core_pattern问题

afl使用中有时会出现以下报错：

![image-20241121113248464](https://typora-images-gqy.oss-cn-nanjing.aliyuncs.com/image-20241121113248464.png)

这是系统配置将core dump发送给了外部工具，afl无法检测到crash信息。运行以下语句即可解决。

```bash
sudo sh -c 'echo core > /proc/sys/kernel/core_pattern'
```

