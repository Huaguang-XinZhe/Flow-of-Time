package com.huaguang.flowoftime

import com.huaguang.flowoftime.test.TrieNode

class KeywordClassifier {
    val root = TrieNode()

    fun insert(keywords: List<String>, category: String) {
        keywords.forEach { keyword ->
            insert(keyword, category)
        }
    }

    fun classify(name: String): String? {
        // 通过嵌套循环的方式遍历所有可能的子串，从前往后，由长到短，找到第一个匹配的关键词就返回
        for (i in name.indices) {
            var node = root
            for (j in i until name.length) {
                node = node.children[name[j]] ?: break
                if (node.isEndOfWord) {
                    return node.category
                }
            }
        }
        return null // 没有找到类属，就返回 null
    }

    private fun insert(keyword: String, category: String) {
        var node = root
        for (ch in keyword) {
            node = node.children.computeIfAbsent(ch) { TrieNode() }
        }
        node.isEndOfWord = true
        node.category = category
    }
}