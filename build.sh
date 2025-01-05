#!/bin/bash

# 构建 Docker 镜像
echo "Building Docker image..."
sudo docker build -t simplecovfuzz .

# 检查构建是否成功
if [ $? -ne 0 ]; then
    echo "Docker image build failed!"
    exit 1
fi

# 运行 Docker 容器
echo "Running Docker container..."
sudo docker run -d -p 8080:8080 --name simplecovfuzz simplecovfuzz

# 检查容器是否运行成功
if [ $? -ne 0 ]; then
    echo "Docker container run failed!"
    exit 1
fi

echo "Docker container is running successfully!"