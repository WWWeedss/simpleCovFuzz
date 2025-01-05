## 任务分配

成员A：顾秋炎221840039

1. 整理任务梳理指标
2. 搭建项目结构
3. 使用afl-cc对所有target进行插装
4. 实现种子排序、能量调度、执行、监控组件

成员B：郑陶211250168

B：

1. 实现评估组件
2. 在target运行测试并记录数据
3. 打包与部署
4. 工具使用方法的演示

## 开发日志

### 插装

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

#### 对于makefile项目

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

#### 对于cmake项目

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

#### readpng

构建完毕后找不到readpng这个可执行文件，在makefile里查找也找不到有readpng这个构建目标。虽然说是找到了readpng.c和readpng.h，尝试用afl-cc进行编译时无法处理链接的一大堆文件，遂放弃。

#### mjs

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

***

### 实现评估组件

负责收集、分析和呈现测试结果的模块。  
通过各种指标和报告，了解测试的覆盖率、效率和软件质量。

#### 评估指标

    代码覆盖率。
    测试用例执行时间：评估测试套件的执行效率。
    测试用例通过率：衡量测试用例的成功率。

#### 具体步骤

    创建CoverageEvalutor类，  
    将覆盖率数据写入文件  
    将其集成到Executor中  
    修改Main类以使用新的Executor函数。  
    添加测速功能  


### 绘制覆盖率曲线 
1.  **导入库:** 导入 `matplotlib.pyplot` 用于绘图。
2.  **`plot_coverage` 函数:**
    *   接收 CSV 文件路径、图表标题和轴标签作为参数。
    *   使用 `pd.read_csv` 读取 CSV 文件到 DataFrame。这里假设 CSV 文件有两列，第一列是 X 轴数据（例如测试用例编号或迭代次数），第二列是 Y 轴数据（覆盖率）。如果你的 CSV 文件没有表头，需要使用`header=None`参数，并使用`names`参数指定列名。如果你的csv文件有表头，则不需要这两个参数。
    *   使用 `plt.plot` 绘制折线图。`marker='o'` 表示在数据点上显示圆圈，`linestyle='-'` 表示使用实线连接数据点，`color='b'`表示使用蓝色线条。
    *   使用 `plt.title`、`plt.xlabel` 和 `plt.ylabel` 添加标题和轴标签。
    *   使用 `plt.grid(True)` 添加网格线。
    *   使用 `plt.xticks` 调整 x 轴刻度，使其更易读。
    *   使用 `plt.tight_layout()` 自动调整子图参数，使之填充整个图像区域。
    *   使用 `plt.show()` 显示图表。
    *   包含 `try...except` 块来处理可能的文件错误，例如文件未找到、文件为空或缺少必要的列。
3.  **`if __name__ == "__main__":` 块:**
    *   设置 CSV 文件路径。
    *   创建示例 CSV 数据并保存到文件（如果文件不存在），方便测试。
    *   调用 `plot_coverage` 函数来绘制覆盖率曲线。

4.  **运行脚本:** 在命令行中运行脚本：`python coverage_plot.py`。

运行后，会显示一个包含覆盖率曲线的图表。收集、处理覆盖率数据并绘制覆盖率曲线。 

### 使用 Docker 构建并推送项目

**1. 环境准备:**

*   **安装 Docker** 确保你的操作系统上安装了 Docker Engine (Linux)。
*   **安装 VS Code Docker 扩展:** 

**2. 创建 Dockerfile:**

在项目根目录下创建一个名为 `Dockerfile` 的文件（没有扩展名）。Dockerfile 包含了构建 Docker 镜像的指令。

```dockerfile
# 使用 ubuntu 镜像作为基础镜像
FROM 

# 设置工作目录
WORKDIR /

# 将构建好的 包复制到容器中
COPY 

# 复制依赖
COPY 

# 设置容器启动时执行的命令
CMD 

# 暴露端口
EXPOSE 
```


**3. 使用 VS Code Docker 扩展构建镜像:**

*   在 VS Code 的侧边栏中点击 Docker 图标 (鲸鱼)。
*   在 "IMAGES" 部分，右键单击你的项目文件夹，选择 "Build Image..."。
*   输入镜像的名称和标签，例如 `your-dockerhub-username/your-app:latest`。
*   VS Code Docker 扩展会自动执行 `docker build` 命令，并显示构建日志。

**4. 登录 Docker Hub:**

```bash
docker login
```

**5. 推送镜像到 Docker Hub:**

```bash
docker push your-dockerhub-username/your-app:latest
```

*   使用 `.dockerignore` 文件排除不必要的文件和目录，例如 `target` 目录和 `.git` 目录，以减小镜像的大小。
*   使用标签 (tags) 来管理不同版本的镜像。
*   使用 Docker Compose 管理多个容器。

### 录制和解说使用视频
