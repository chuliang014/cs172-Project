import http
import json
import os
import queue
import re
import sys
import threading
import urllib.request as urllib

import tweepy
from urllib3.exceptions import ProtocolError, ReadTimeoutError

consumer_key = 'JsLfvl8gYTbAjozeRvBP5SL9t'
consumer_secret = 'PnLhX1F5Lnv4jyMm2bWxjp6jIleUDEKuZVumjqsjer924ZURrO'
access_token = '743234403669684227-9d6jzlupZ9k0DfVONXjdswMsJ0sieTq'
access_token_secret = 'wbgftRYRCFnckpTl1jOZjFOKAZLVtheiSbSPeJAEbP4tC'

auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token, access_token_secret)

api = tweepy.API(auth, timeout=30)

currFile = 0
DIR_NAME = "twitterdata"
if not os.path.exists(DIR_NAME):
    os.makedirs(DIR_NAME)
save_path = DIR_NAME + '/'
fileName = os.path.join(save_path + 'tweetDB_' + str(currFile) + '.json')
tweetDB = open(fileName, 'a')

max_size = 10 * 1024 * 1024  # 10485760
maxNumFiles = 200  # 220
tweets_data = []
NUM_TWEETS = 10


class MyStreamListener(tweepy.StreamListener):

    def __init__(self, q=queue.Queue()):
        # initialize 4 tread for processing data of queue
        super(MyStreamListener, self).__init__()
        num_worker_threads = 4
        self.q = q
        for i in range(num_worker_threads):
            t = threading.Thread(target=self.do_stuff)
            t.daemon = True
            t.start()

    def on_status(self, status):
        data = status._json
        # just collect data in this function and no other processing
        # because it may cause program stall or exception like incompleteRead
        self.q.put(data)
        return True

    # this is for processing the data
    def do_stuff(self):
        while True:
            tweets_data = self.q.get()
            print(tweets_data)
            process(tweets_data)
            self.q.task_done()

    def on_error(self, status_code):
        if status_code == 420:
            # returning False in on_error disconnects the stream
            return False


def process(tweets_data):
    global fileName, tweetDB, currFile
    try:
        temp_text = tweets_data['text']
        # print(tweets_data['text'])
        text = temp_text.encode('ascii', 'ignore')
        decoded_text = text.decode('utf8')
        tweets_data['title'] = None
        # judge if the title include http
        # if True, parsing the url to get the title.
        if "http" in tweets_data['text']:
            text = decoded_text
            url = re.search("(?P<url>https?://[^\s'\"]+)", text).group("url")
            tweets_data['urls'] = url
            temp_urls = url.encode('ascii')
            urls = temp_urls.decode('utf8')
            titles = []
            try:
                webpage = urllib.urlopen(str(urls)).read().decode('utf8')
                if "<title>" in webpage:
                    titles.append(webpage[webpage.find("<title>"):webpage.find("</title>")][7:])
                    tweets_data['title'] = titles
                else:
                    tweets_data['title'] = None
            except UnicodeEncodeError:
                print("vincent UnicodeEncodeError inner")
                pass
            except UnicodeDecodeError:
                print("vincent UnicodeDecodeError inner")
                pass
            except IOError:
                print("vincent IOError")
                pass
            except http.client.IncompleteRead:
                print("vincent incompleteread")
                pass
            except http.client.InvalidURL:
                print("vincent invalidUrl")
                pass
            except ProtocolError:
                print("Vincent ProtocolError")
                pass

        tweetDB.write(json.dumps(tweets_data) + '\n')

        # if reach 2G, teminate the program
        if currFile > maxNumFiles:
            myStream.disconnect()

        # check if current file is over 10MB
        # if True, close file and create a new file for collecting
        statinfo = os.stat(fileName)
        if statinfo.st_size > max_size:
            tweetDB.close()
            currFile += 1
            fileName = os.path.join(save_path + 'tweetDB_' + str(currFile) + '.json')
            tweetDB = open(fileName, 'a')
    except UnicodeEncodeError:
        print("vincent UnicodeEncodeError outer")
        pass
    except ProtocolError:
        print("Vincent ProtocolError")
        pass
    except http.client.IncompleteRead:
        print("vincent incompleteread")
        pass
    except Exception as e:
        print('vincent error {}'.format(e))
        pass

#initialize stream
myStreamListener = MyStreamListener()
myStream = None
myStream = tweepy.Stream(auth=api.auth, listener=myStreamListener)

def startFilter(stream, **kwargs):
    try:
        stream.filter(**kwargs)
    except ReadTimeoutError:
        stream.disconnect()
        print("vincet startFilter ReadTimeout error")
        startFilter(stream, **kwargs)
    except Exception:
        stream.disconnect()
        print("Fatal exception. Consult logs.")
        startFilter(stream, **kwargs)


startFilter(myStream, locations=[-131.594367, 13.182335, -48.743831, 71.434357], stall_warnings=True,is_async=True)

