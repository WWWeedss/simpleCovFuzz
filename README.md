### 项目结构

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

#### 执行流程

![image-20241122173159232](https://typora-images-gqy.oss-cn-nanjing.aliyuncs.com/image-20241122173159232.png)

关于执行流程的解释：我希望任何种子只执行一次，对于已经执行过的种子，即"processedSeed"，只留有变异功能，而不再进入seedsQueue被执行。而只有执行过的种子才会有数据，种子排序组件和能量调度组件根据覆盖度进行作用，相应的也就是对processedSeedsList进行作用，它们会影响种子的变异优先级以及变异后代数量。

所以，我让种子排序组件和能量调度组件在执行之后再进行作用。

### 使用方法

#### 环境配置

操作系统：Ubuntu-22.04

##### 安装afl

```bash
git clone https://github.com/AFLplusplus/AFLplusplus.git
cd AFLplusplus
make
```

##### 其他依赖

我还没有在其他机器上测试过，此项待定

```bash
sudo apt update
sudo apt install zlib1g-dev
```

ubuntu 似乎自带python 

还有docker

### start

运行java项目，在控制台输入待测数据目录  
得到csv文件后运行analyze_courage.py生成覆盖图

#### 输出目录

默认输出目录为src/main/java/org/example/output_dir

其中/crash记录崩溃种子的信息，/queue记录所有执行过的种子的信息，/mutate-seed用来存放变异的种子