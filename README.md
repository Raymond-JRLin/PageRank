# PageRank
Implemented PageRank algorithm used by Google Search with MapReduce to rank websites in search engine results

## What's PageRank
In one word, PageRank is a n algorithm used by Google Search to rank websites in their search engine results.

## How it works
Other than tranditional websites ranking method, PageRank involes all website to do ranking. There are 2 basic theories behind<sup>[1]</sup>:
1. More importatnt websites are more likely to received more links from other websites
2. Websites with higher PageRank will pass higher weight

![](https://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/PageRanks-Example.svg/758px-PageRanks-Example.svg.png)
> Mathematical PageRanks for a simple network, expressed as percentages.<sup>[2]</sup>

So how to represent these 2 theories in our algorithm? We need 2 things: representation of directivity among pages, and representation of importance (weight) of each websites.

I use following pictures to give an example:

![](https://github.com/Raymond-JRLin/Improved-PageRank-with-Beta-Parameter/blob/master/images/example.png?raw=true)
> Example 1

### 1. How to represent directivity among pages

In above example, we have following transition:

```
A -> B, C, D
B -> A, D
C -> A
D -> B, C
```

Assuming that the possibility of a transition from one page to itself as 0, and they have equal possibilities to jump to any other pages they can, then we will have following **transition matrix**:

|To \ From  | A         | B         | C         | D         |
|   :---:   |   :---:   |   :---:   |   :---:   |   :---:   |
| A         |     0     |     1/2   |     1     |     0     |
| B         |    1/3    |     0     |     0     |     1/2   |
| C         |    1/3    |     0     |     0     |     1/2   |
| D         |    1/2    |     1/2   |     0     |     0     |

### 2. How to represent the importance (weight) of each website

Actually, PageRank is the name of this algorithm, but also it's ranking results, thus we can just use PageRank of each website to show their importance.

Assuming A, B, C and D has same initialized PageRank, we can have following **PageRank Matrix**:

|  website  |    PR0    | 
|   :---:   |   :---:   | 
| A         |    1/4    | 
| B         |    1/4    | 
| C         |    1/4    | 
| D         |    1/4    |

*Note: PR0 of A, B, C and D can be initialized as 1 for each, which means their absolute values are not important but relative values.*

## Calculation

From above part, we know the mechanism and theories of PageRank, so how can we get the final PageRank results?

If we know Page-B have *1/2* change to jump to Page-A, and B has *PRO == 1/4*, then B -> A will give A *(1/2 * 1/4 = 1/8)* weight.

Keep doing this on Page-C, Page-D to Page-A, we can have PR1 of Page-A. Also, we can get PR1 of Page-B,C and D:

|  website  |    PR1    | 
|   :---:   |   :---:   | 
| A         |    9/24   |
| B         |    5/24   | 
| C         |    5/24   | 
| D         |    5/24   |

So we can use matrix multiplication to get PR1, PR2, ... , PRN:

```
PR1 = Transition Matrix * PR0
PR2 = Transition Matrix * PR1
PR3 = Transition Matrix * PR2
...
PRN = Transition Matrix * PR(N-1)
```

It's a iteration process. In each iteration, we transit weight from each page to other pages, so more important pages will receive more weight, but less important pages will have less weigth. Finally, the difference between these two kinds pages will show.

When shall we stop iterations? According to a [research](https://projects.ncsu.edu/crsc/reports/ftp/pdf/crsc-tr04-02.pdf), the PageRank matrix will finally converge within 30 - 40 times iteration.<sup>[3]</sup>


## Data

The [smallTestData](https://github.com/Raymond-JRLin/Improved-PageRank-with-Beta-Parameter/tree/master/smallTestData) is what I created to test Example 1, including 4 websites.

The [bigTestData](https://github.com/Raymond-JRLin/Improved-PageRank-with-Beta-Parameter/tree/master/bigTestData) concludes 6012 webpages, source comes from this [page](https://www.limfinity.com/ir/).

Each test data contains 2 files: pr.txt, which is PRO, and transition.txt, which is transition matrix.

## Prerequisite

You need to have [Docker](https://store.docker.com/search?type=edition&offering=community) installed in your laptop.

For Mac: download [here](https://docs.docker.com/docker-for-mac/)

For Linux:

```
sudo apt-get update
sudo apt-get install wget
sudo curl -sSL https://get.daocloud.io/docker | sh # users in mainland of China can use instead: sudo curl -sSL https://get.docker.com (https://get.docker.com/) | sh
sudo docker info
```

## How to run

### 1. Run Docker

If it shows 

>docker is running

, it means you already ran docker successfully.

For users in mainland of China, you can consider using acceleration service provided by cloud tech company.

### 2. Clone this repo to ~/src
```
cd ~/src # open ~/src, if there doesn't exist, then use mkdir to create one
git clone https://github.com/Raymond-JRLin/Improved-PageRank-with-Beta-Parameter.git
```

### 3. Create a new directory wherever you want run this project, not in ~/src
```
mkdir pagerank
cd pagerank
```

### 4. Create Hadoop cluster nodes (for Mac/Linux)
```
docker pull joway/hadoop-cluster # pull docker image
git clone https://github.com/Raymond-JRLin/Hadoop-Cluster-Docker.git # clone related code
sudo docker network create --driver=bridge hadoop #create a bridge for hadoop nodes to communicate
```

### 5. Run Hadoop
```
cd hadoop-cluster-docker
./start-container.sh # open and enter docker container
./start-hadoop.sh # start Hadoop
```
If it shows 

>root@hadoop-master:#

, it means you already in docker and run Hadoop right now.

### 6. Run PageRank

*Attention: I have small and big test data, before you run, copy data files you want to test into ```src/main/java```, and use corresponding file names.*

```
cd src/main/java/ # enter project source code
hdfs dfs -rm -r /transition # remove /transition directory in hdfs, if there's not such dir, then it will give you an error, ignore it; however you need to make sure there's no such dir when you run this project everytime
hdfs dfs -mkdir /transition # create /transition directory int hdfs
hdfs dfs -put transitions.txt /transition # copy transisitons.txt to /transition in hdfs
hdfs dfs -rm -r /output* # remove /output directory in hdfs, if there's not such dir, then it will give you an error, ignore it; however you need to make sure there's no such dir when you run this project everytime
hdfs dfs -rm -r /pagerank* # remove /pagerank directory 
hdfs dfs -mkdir /pagerank0 # create /pagerank0 directory
hdfs dfs -put prsmall.txt /pagerank0 # upload PR0
hadoop com.sun.tools.javac.Main *.java # complie java source codes
jar cf pr.jar *.class # make all class file into a jar file
hadoop jar pr.jar Driver /transition /pagerank /output 1 # run jar
// args0: dir of transition.txt
// args1: dir of PageRank.txt
// args2: dir of unitMultiplication result
// args3: times of convergence（make sure the code run successfully when args3=1, then test args3=40）
```

### 7. Check results
The result will be stored in ```/pagerankN``` directory, N is the iteration times.
Use following order to check results:
```
hdfs dfs -cat /pagerank1/* # this is for test of args3=1
```
Then you can use big data to test.

### 8. Download results if you want
```
hdfs dfs -get <src> <localDest> # src: the addresss of original file you want download, localDest: name of download file you wanna give
```

## Improvement

Please check [Improved PageRank](https://github.com/Raymond-JRLin/Improved-PageRank-with-Beta-Parameter/tree/master#improvement) for my improved repo.


## Reference
1. [The PageRank Citation Ranking: Bringing Order to the Web](http://ilpubs.stanford.edu:8090/422/1/1999-66.pdf)
2. [Wikipedia](https://en.wikipedia.org/wiki/PageRank)
3. [Convergence Analysis of an Improved PageRank Algorithm](https://projects.ncsu.edu/crsc/reports/ftp/pdf/crsc-tr04-02.pdf)

