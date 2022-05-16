import jieba
from wordcloud import WordCloud


def main():
    filename = r"./input.txt"

    # 感恩
    jieba.enable_parallel(16)

    with open(filename, 'r', encoding='utf-8') as f:
        final = "/ ".join(jieba.cut(f.read()))

    excludes = {
        '的', '了', '是', "我", '你', "有", "和", "也", '都', '吗', '被',
        '没', '用', '能', '吧', '额', '要', '他', '她', '又', '就', '那', '吧', '呢',
        'com', 'https',
        '我们', '你们', '他们', '它们', '因为', '因而', '所以', '如果', '那么',
        '如此', '只是', '但是', '就是', '这是', '那是', '而是', '而且', '虽然',
        '这些', '有些', '然后', '已经', '于是', '一种', '一个', '一样', '时候',
        '没有', '什么', '这样', '这种', '这里', '不会', '一些', '这个', '仍然', '不是'
    }

    word_pic = WordCloud(
        font_path=r'/Users/col/Library/Fonts/LXGWWenKaiMono-Bold.ttf',
        width=2000,
        height=2000,
        min_font_size=20,
        max_font_size=400,
        max_words=500,
        stopwords=excludes,
        repeat=False,
        background_color='white'
    ).generate(final)

    word_pic.to_file(r'./output.png')

    # plt.imshow(word_pic, interpolation='bilinear')
    # plt.axis('off')
    # plt.savefig(r'./output.png')


if __name__ == "__main__":
    main()
