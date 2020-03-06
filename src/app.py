from flask import Flask, render_template,request
from configparser import SafeConfigParser
import socket
#import requests
import subprocess
import os

app = Flask(__name__)
parser = SafeConfigParser()

#metadata_server = "http://metadata/computeMetadata/v1/instance/"
#metadata_flavor = {'Metadata-Flavor' : 'Google'}
#gce_id = requests.get(metadata_server + 'id', headers = metadata_flavor).text
#gce_name = requests.get(metadata_server + 'hostname', headers = metadata_flavor).text
#gce_machine_type = requests.get(metadata_server + 'machine-type', headers = metadata_flavor).text

@app.route('/')
def index():
    containerId=subprocess.check_output('cat /proc/self/cgroup | grep kubepods | cut -d "/" -f 4 | tail -1', shell=True, encoding='utf-8').strip()
    hostname = socket.gethostname()
    IPAddr = socket.gethostbyname(hostname) 
    clientIP = request.remote_addr
    requestMethod = request.method
    requestPath = request.path
    projectID = os.environ['PROJECTID']

    return render_template('index.html', containerId=containerId, hostname=hostname, IPAddr=IPAddr, clientIP=clientIP, requestMethod=requestMethod, requestPath=requestPath, projectID=projectID)

@app.route('/alive')
def alive():
    return "Yes"

@app.route('/hello/<name>')
def hello(name=None):
    return render_template('hello.html',
                           greeting=parser.get('features', 'greeting', fallback="Howdy"),
                           name=name)

if __name__ == "__main__":
    app.run(host="0.0.0.0")
