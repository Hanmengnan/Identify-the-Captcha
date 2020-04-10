import random
import math

import uuid
import numpy
from PIL import Image, ImageFont, ImageDraw

VERIFY_CODES = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZ"


class captcha:
    def __init__(self):
        self.color = [(0, 0, 0), (255, 0, 0), (255, 255, 0), (0, 0, 255)]
        self.filename = ""
        self.pic_height = 35
        self.pic_width = 90

    def choose_char(self):
        """
        生成验证码文字
        """
        captcha_str = random.sample(VERIFY_CODES, 6)
        return captcha_str

    def random_color(self):
        """
        随机颜色
        """
        return (random.randint(100, 255), random.randint(100, 255), random.randint(100, 255))

    def word_font(self):
        """
        文字字体
        """
        return ImageFont.truetype(r'F:\PythonProject\Identify-the-Captcha\Other\actionj.ttf', 19)
        # 这里一定要用绝对路径

    def draw_char(self, aim_char, color):
        """
        单独生成一个字母的图像，再进行旋转
        """
        angle = random.randint(-45, 45)

        char_img = Image.new("RGBA", (20, 20), color=(255, 255, 255, 255))
        char_pen = ImageDraw.Draw(char_img)
        char_pen.text((0, 0), aim_char, fill=color, font=self.my_front)
        char_img = char_img.rotate(angle)

        fff = Image.new('RGBA', (20, 20), (255, 255, 255, 255))
        # 使用alpha层的rot作为掩码创建一个复合图像
        out = Image.composite(char_img, fff, char_img)
        out = out.convert('RGB')
        return out

    def shearY(self):
        """
        扭曲图像
        """
        period = random.randint(0, 40)+10
        frame = 20
        phase = 7

        are_color = self.random_color()

        for i in range(self.pic_width):
            d = (float(period))/2 * math.sin(float(i) /
                                             float(period)+math.pi*phase/frame)
            d = int(d)
            area = (i, 0, 1, self.pic_height)
            part = self.graphic.crop(area)

            self.graphic.paste(part, (i, d))
            self.pen.line([(i, d), (i, 0)], are_color)
            self.pen.line(
                [(i, d+self.pic_height), (i, self.pic_height)], are_color)

    def noisy_point(self):

        point_num = int(0.02 * self.pic_height*self.pic_width)
        for i in range(point_num):
            x = random.randint(1, self.pic_width-2)
            y = random.randint(1, self.pic_height-2)
            random_rgb = (random.randint(0, 255), random.randint(
                0, 255), random.randint(0, 255))
            self.pen.point([(x, y)], random_rgb)

    def noisy_linear(self):
        linear_num = random.randint(1, 3)
        for i in range(linear_num):
            x1 = random.randint(0, self.pic_width)
            y1 = random.randint(0, self.pic_height)
            x2 = random.randint(0, self.pic_width)
            y2 = random.randint(0, self.pic_height)
            self.pen.line([(x1, y1), (x2, y2)], (0, 255, 0), 1)

    def output_image(self, need_color=(0, 0, 0)):
        self.filename = ""

        captcha_str = self.choose_char()

        aim_char_num = random.randint(3, 4)

        aim_char_index = random.sample([0, 1, 2, 3, 4, 5], aim_char_num)

        self.graphic = Image.new("RGB", (self.pic_width, self.pic_height))

        self.pen = ImageDraw.Draw(self.graphic)

        self.my_front = self.word_font()

        background_color = self.random_color()
        # 边框
        edge_point = []
        for i in range(self.pic_width):
            edge_point.append((i, 0))
            edge_point.append((i, self.pic_height-1))
        for i in range(self.pic_height):
            edge_point.append((0, i))
            edge_point.append((self.pic_width-1, i))
        self.pen.point(edge_point, background_color)

        # 背景
        for i in range(self.pic_width):
            for j in range(self.pic_height):
                self.pen.point([(i, j)], background_color)

        # 扭曲
        self.shearY()

        # 文字
        for i in range(6):
            x = (self.pic_width-10)/6*i+5
            y = + (self.pic_height - 4) / 2 - 8

            if i in aim_char_index:
                color = need_color
                self.filename += captcha_str[i]
            else:
                other = list(set(self.color)-set([need_color]))
                color = random.choice(other)

            char_img = self.draw_char(captcha_str[i], color)
            #单个字符图片
            img_vector = numpy.array(char_img)
            #图片矩阵

            (m, n, r) = img_vector.shape

            for j in range(m):
                for k in range(n):
                    rgb_list = list(img_vector[j][k])

                    color_depth = (rgb_list[0]*0.299 +
                                   rgb_list[1]*0.587+rgb_list[2]*0.114)
                    #灰度方式计算颜色深浅
                    base_depth = (color[0]*0.299+color[1]*0.587+color[2]*0.114)
                    #基础颜色深浅
                    if color == (255, 255, 0):
                        #黄色本身就比较浅，所以另行计算
                        if rgb_list[2] < 160:
                            #偏深黄色
                            self.pen.point([(x+k, y+j)], tuple(color))

                    else:
                        if color_depth > 200:
                            #浅
                            pass
                        elif color_depth < 100:
                            #深
                            self.pen.point([(x+k, y+j)], color)
                        else:
                            #相对
                            rgb = numpy.array(
                                img_vector[j][k])*(base_depth/color_depth)+numpy.array(color)*(1-base_depth/color_depth)

                            rgb = rgb.astype(int)
                            #rgb需要为整数
                            self.pen.point([(x+k, y+j)], tuple(rgb))
        # 噪点
        self.noisy_point()
        # 干扰线
        self.noisy_linear()

        return self.graphic, self.filename


if __name__ == "__main__":
    my_captcha = captcha()
    for i in range(10000):
        filepath = "./img/red/"
        img, filename = my_captcha.output_image(
            (255, 0, 0))
        filepath += filename+"_"+str(uuid.uuid4())+".png"

        img.save(filepath, "png")
