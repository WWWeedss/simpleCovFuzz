视频链接：【软件测试大作业】https://www.bilibili.com/video/BV1zTrKYoED7?vd_source=85828480e5dd422bda867bd6a3818cd1

## 项目结构

```bash
├─.idea
└─src
    └─main
        └─java
            └─org
                └─example
                    ├─fuzzer
                    │      EnergyScheduler.java #能量调度组件
                    │      Executor.java        #执行组件，负责调度其他组件
                    │      Logger.java          #日志组件，负责将种子数据输出到文件夹，默认输出文件夹是output_dir
                    │      Mutator.java         #变异组件
                    │      Seed.java            #种子数据类
                    │      SeedSorter.java      #种子排序组件

                    ├─fuzz_targets              #插装好的待测程序
                    │  ├─target1
                    │  ├─target10
                    │  ├─target2
                    │  ├─target3
                    │  ├─target4
                    │  ├─target5
                    │  ├─target7
                    │  ├─target8
                    │  └─target9 
                    └─input                      #待测种子
                        ├─target10seed
                        ├─target1seed
                        ├─target2seed
                        ├─target3seed
                        ├─target4seed
                        ├─target5seed
                        ├─target7seed
                        ├─target8seed
                        └─target9seed、
```

![image-20241122172626432](https://typora-images-gqy.oss-cn-nanjing.aliyuncs.com/image-20241122172626432.png)

### 执行流程

![image-20241122173159232](https://typora-images-gqy.oss-cn-nanjing.aliyuncs.com/image-20241122173159232.png)

关于执行流程的解释：我希望任何种子只执行一次，对于已经执行过的种子，即"processedSeed"，只留有变异功能，而不再进入seedsQueue被执行。而只有执行过的种子才会有数据，种子排序组件和能量调度组件根据覆盖度进行作用，相应的也就是对processedSeedsList进行作用，它们会影响种子的变异优先级以及变异后代数量。

所以，我让种子排序组件和能量调度组件在执行之后再进行作用。

## 使用方法

### 环境配置

操作系统：Ubuntu-22.04

**提供了Dockerfile和可一键构建的脚本 build.sh**

确认脚本具有可执行权限

```bash
chmod +x ./build.sh
```

运行

```bash
./build.sh
```

构建成功出现Docker container is running successfully!

```bash
git clone https://github.com/AFLplusplus/AFLplusplus.git
cd AFLplusplus
make
```

```bash
sudo apt update
sudo apt install zlib1g-dev
```

### start

```bash
//原先是在控制台输入待测数据和参数，
//为了节约运行时间就直接同时运行10个样本了
//直接点击运行即可
```

### 测试target

原理是调用以下命令：

```bash
afl-showmap -o - -- …
```

…即运行可执行文件的命令，这种运行和在终端内直接运行差别不大，因此运行可执行文件的命令行参数是必要的，以下是可测试的target的输入

至于input files则是询问需不需要将种子文件的内容读取并写入可执行文件的输入流。

```bash
#提示输入
Please enter the target path:

Please enter the seed directory path:

Do you need to provide extra arguments for the target program? (y/n)

Are the seeds input files? (y/n)
```

#### target1

```bash
/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/fuzz_targets/target1/cxxfilt

/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/input/target1seed

n

y
```

#### target2

```bash
/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/fuzz_targets/target2/readelf

/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/input/target2seed

y

-a #这是命令行参数，可选，详情请参考readelf的具体使用方式。下方使用到命令行参数的同此

n
```

#### target3

```bash
/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/fuzz_targets/target3/nm-new

/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/input/target3seed

n

n
```

#### target4

```bash
/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/fuzz_targets/target4/objdump

/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/input/target4seed

y

-a

n
```

#### target5

```bash
/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/fuzz_targets/target5/djpeg

/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/input/target5seed

y

-verbose

n
```

#### target6

插桩没搞定，遂鸽了

#### target7

```bash
/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/fuzz_targets/target7/xmllint

/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/input/target7seed

y

--noout

n
```

#### target8

```bash
/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/fuzz_targets/target8/lua

/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/input/target8seed

n

n
```

#### target9

插桩没搞定，虽然mjs能运行，但是afl-showmap没法做。

#### target10

```bash
/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/fuzz_targets/target10/tcpdump

/home/wwweeds/fuzzTestDir/simpleCovFuzz/src/main/java/org/example/input/target10seed

y

-r 

n
```

### 

#### 输出目录

终端会持续输出实时测试信息  
速度，覆盖度等

默认输出目录为  
src/main/java/org/example/output_dir

其中/crash记录崩溃种子的信息，  
/queue记录所有执行过的种子的信息，  
/mutate-seed用来存放变异的种子

测试输出得到csv文件（覆盖数据）

运行analyze_courage.py生成覆盖图  
与其他测试软件的输出对比分析
