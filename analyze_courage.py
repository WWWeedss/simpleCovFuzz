import matplotlib.pyplot as plt
import csv

def read_coverage_data(file_path):
    executions = []
    coverages = []
    with open(file_path, 'r') as csvfile:
        reader = csv.reader(csvfile)
        next(reader)  # 跳过标题行
        for row in reader:
            executions.append(int(row[0]))
            coverages.append(int(row[1]))
    return executions, coverages

def plot_coverage_curve(executions, coverages):
    plt.figure(figsize=(10, 6))
    plt.plot(executions, coverages, marker='o', linestyle='-', color='b')
    plt.xlabel('Execution')
    plt.ylabel('Coverage')
    plt.title('Coverage Curve')
    plt.grid(True)
    plt.savefig('src/main/java/org/example/output_dir/coverage_curve.png')  # 保存图表为文件
    plt.show()

if __name__ == "__main__":
    file_path = 'src/main/java/org/example/output_dir/coverage_curve.csv'
    executions, coverages = read_coverage_data(file_path)
    plot_coverage_curve(executions, coverages)