# ExtClipboardManager

一个增强的剪切板管理器，起因是我在OPPO开发者平台看到`Android13`的新功能中有一项是剪切板自动清除的功能，感觉对保护隐私这一方面还是有点益处的，但是又不是很彻底。比如说一个应用可以在剪切板自动清除的这一段时间内反复蹂（qiang）躏（jian）你的剪切板，特别点名国内某些大厂的应用，你不给它读吧，它有时候又是需要的;你给它读吧，你又不知道它除了自己需要的那些数据之外还读了写什么东西，特别是在一些大数据推荐的场景中（就一个字，恶心，我都关着灯，还当着人家的面，这种事哪怕你们多花点钱呢）。

# 原理

其实就是在`ClipboardService`启动的时候进行代码注入，在`setPrimaryClip`等关键方法执行之前或之后进行拦截、标记等处理。

# 目前功能

+ 超时自动清除剪切板：可自定义设置时间，以秒为单位
+ 提供应用白名单与黑名单模式：白名单模式下白名单内的应用不会触发清除剪切板操作;黑名单模式下黑名单应用才会触发清除剪切板操作
+ 文本排除功能：匹配规则的文本不会被自动清除

# 注意事项

+ 输入法有可能会在剪切之后读取多次剪切板内容，白名单模式下推荐将输入法加入白名单，不然读取次数很容易就被输入法给耗尽了。
+ 某些情况下不会触发`getPrimaryClip`读取剪切板但是应用也能照样读取剪切板，可能要有空看下源码之后才能知道是什么原因

# License
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```