"""
This script analyzes and visualizes code coverage data from CSV files.
It reads execution counts and coverage metrics, then generates coverage curve plots.
"""

import matplotlib.pyplot as plt
import csv

def read_coverage_data(file_path):
    """
    Read coverage data from a CSV file.
    
    Args:
        file_path (str): Path to the CSV file containing coverage data
        
    Returns:
        tuple: Two lists containing execution counts and coverage values
    """
    executions = []  # List to store execution counts
    coverages = []   # List to store coverage values
    
    with open(file_path, 'r') as csvfile:
        reader = csv.reader(csvfile)
        next(reader)  # Skip header row
        for row in reader:
            # Parse and store execution count and coverage from each row
            executions.append(int(row[0]))
            coverages.append(int(row[1]))
    return executions, coverages

def plot_coverage_curve(executions, coverages):
    """
    Generate and save a coverage curve plot.
    
    Args:
        executions (list): List of execution count values
        coverages (list): List of coverage values
    """
    # Create new figure with specified size
    plt.figure(figsize=(10, 6))
    
    # Plot coverage curve with markers and line
    plt.plot(executions, coverages, marker='o', linestyle='-', color='b')
    
    # Add labels and title
    plt.xlabel('time')
    plt.ylabel('Coverage')
    plt.title('Coverage Curve')
    
    # Add grid for better readability
    plt.grid(True)
    
    # Save plot to file
    plt.savefig('src/main/java/org/example/output_dir/thread_0/coverage_curve.png')
    
    # Display the plot
    plt.show()

if __name__ == "__main__":
    # CSV file path containing coverage data
    file_path = 'src/main/java/org/example/output_dir/thread_0/coverage_curve.csv'
    
    # Read data from CSV file
    executions, coverages = read_coverage_data(file_path)
    
    # Generate and display coverage curve
    plot_coverage_curve(executions, coverages)