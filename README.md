# Flow-of-Time
时光流

### 简介

这是我写的时间最长，花费精力最多的一个软件，经历多次重构和一次重大版本更新。

这是第一版：

<div style="display: flex; justify-content: space-around">
  <img
    src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/mmexport1708091900105.jpg"
    alt="#"
    style="width: 200px"
  />
  <img
    src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/mmexport1708091900311.jpg"
    alt="#"
    style="width: 200px"
  />
  <img
    src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/mmexport1708091899874.jpg"
    alt="#"
    style="width: 200px"
  />
</div>

这是第二版，也是现在在用的：

<div style="display: flex; justify-content: space-around">
  <img
    src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/mmexport1708094273129.jpg"
    alt="#"
    style="width: 200px"
  />
  <img
    src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/Screenshot_2024-02-16-22-18-24-55_31f4652e2987e4ef63b6ca8eda686377.jpg"
    alt="#"
    style="width: 200px"
  />
  <img
    src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/Screenshot_2024-02-16-22-04-01-40_31f4652e2987e4ef63b6ca8eda686377.jpg"
    alt="#"
    style="width: 200px"
  />
</div>

<div style="display: flex; justify-content: space-around">
  <img
    src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/mmexport1708091900937.jpg"
    alt="#"
    style="width: 200px"
  />
  <img
    src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/mmexport1708094264089.jpg"
    alt="#"
    style="width: 200px"
  />
</div>

在确定开发（二版）之前，也曾想过这样的设计（不过没有采用）：

<img src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/mmexport1708091900523.png" alt="mmexport1708091900523" style="zoom: 33%;" />

现在呢，已经酝酿并构建好了第三版的设计（代码效果呈现，非设计稿），但还来不及开发：

<img src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/mmexport1708091900797.jpg" alt="mmexport1708091900797" style="zoom:33%;" />

> Tip：请关注上图的样式，内容是随意输入的。



它是一个时间记录、分析工具，可以自由地记录（24h，全记录）每天的事项、活动，然后类属分析。

记录的过程本身能让人保持专注。一般而言，事项一旦开始，就得设置一个主题（可点击修改），然后在这个主题下活动，不能主动逾越。如果被迫中断，跳出主题，则执行插入（记录），结束后又回到主题。如果不想继续主题，那就直接结束，然后开启一个新事项即可。

如果错过记录，可长按 “开始” 按钮进行补记，这会在上一事项的结束时间后加两分钟开启一个新事项。

「时光流」提供了这样的层级显示（双击卡片可切换记录模式和展示模式）：

<div style="display: flex; justify-content: space-around; overflow: auto">
  <img
    src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/Screenshot_2024-02-17-07-56-24-33_31f4652e2987e4ef63b6ca8eda686377.jpg"
    alt="#"
    style="zoom: 28%;"
  />
  <img
    src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/Screenshot_2024-02-17-07-56-58-78_31f4652e2987e4ef63b6ca8eda686377.jpg"
    alt="#"
    style="zoom: 28%;"
  />
</div>

记录完后，可立即指定类属，亦可在在展示页集中指定，即使是在统计页，也能够非常方便的修改（相关的统计数据也会跟随变化）。

### 背景

19 年高考结束后，我广泛涉略（阅读），忘记是从哪本书上了解到的 “时间记录” 的概念，逐步开始实践。

最初，我是用存粹的纸笔进行记录的，事无巨细，如实呈现（当然，低于 5 分钟的活动忽略）。

一天就是一张纸，第二天一早就复盘分析。看看自己的时间花在哪几个方向，都花了多少时间；思考有哪些事项是不必要或应该减省的，哪些事项应该加大投入，等等。

记录和分析会订在一起，放入档案袋中，以供周、月汇总分析（这方面执行不到位，做的不是很好）。

这样做了几个月，我就发现：

- 记录时常遗漏，不完整（尽管我随身准备了纸笔，时刻准备记录）；
- 维护耗时巨大，有时会因懒惰或情绪而中断，造成 “崩溃”；

但当时也没什么好办法，就这么断断续续地维持着。直到我有了电脑，接触并学习了 Excel。

于是，记录逐渐从线下转移到了线上（下图是第一次记录的情况）。

![image-20240218194350114](https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/image-20240218194350114.png)

统计、分析也是（后期实现）。

<img src="https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/image-20240218195125638.png" alt="image-20240218195125638" style="zoom: 67%;" />

这极大地提高了时间记录、统析的效率。

我记的完整多了，统析也丰富了许多。

但还是有一些问题：

1. **转录麻烦**。由于在移动端的 WPS 中记录时间不太方便、自然，我就用手机自带的便签代替。但这些记录每天还都需要转录到 Excel 中，比较麻烦；
2. **不能自动类属**。时间记录中的事项都是详尽而具体的活动，这是我的要求，以便于后期回溯。但它并不利于统计分析，要想利用 Excel 自带的汇总计算功能，我就必须给每一个具体事项指定一个简短的类属，这个类属能概括一些同类事项。
   比如 “吃晚饭”、“洗澡”、“拿快递”、“厕” 等都可以类属为 “常务”，这是有穷且显而易见的。如果遇到这些关键词，应该要自动打标类属才是，而不应该每次都手动指定。
3. **移动端不好运行**。只能在 PC 端维护，移动端不好运行。这在独立、稳定的环境下没什么问题，但在共居、变化的环境下就不太好了，因为有时候忙起来，连打开电脑的机会都没有！就算有时间，受限于共居环境，用电脑办公时也往往难以专注。而手机就不一样了，可以随身携带，在任何适宜的环境下开始。
4. **重启、维护费时**。有时候会一连落下好几天，等有时间可以重启的时候，一想到转录、打标、汇总、分析这一条龙下来可能要花费好几个小时，在多个软件中反复横跳，就不愿意动手了。这当然是我自己的问题，但维稳成本确实有点高。

我写过记录解析程序（Java 实现，复制粘贴即可实现转录），了解过 VBA（浅），想尽办法进行优化。但始终觉得不完美，一旦觉得不完美了，在维护上就不太愿意全情投入了，慢慢的也就松懈了下来。这直接导致记录、统析的不完善，时断时续的，感觉又回到了以前的状态。

Excel 记录、统析的方式一直持续到 23 年 5 月，彼时我已经处于独立、稳定的生活状态，正在学习 Compose（一个新的 Android 声明式 UI 框架），刚刚结束「[吃什么](https://mp.weixin.qq.com/s/FA-S0EcZ1MIhuvz-pseeaA)」程序的开发。

至此，我终于有能力尝试「时光流」的开发了。

