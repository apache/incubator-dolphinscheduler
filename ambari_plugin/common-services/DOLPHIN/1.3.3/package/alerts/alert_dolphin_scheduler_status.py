"""
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import socket
import urllib2
import os
import logging
import ambari_simplejson as json
from resource_management.libraries.script.script import Script
import sys
reload(sys)
sys.setdefaultencoding('utf-8')

logger = logging.getLogger('ambari_alerts')

config = Script.get_config()


def get_tokens():
    """
    Returns a tuple of tokens in the format {{site/property}} that will be used
    to build the dictionary passed into execute

    :rtype tuple
    """

def get_info(url, connection_timeout):
    response = None

    try:
        response = urllib2.urlopen(url, timeout=connection_timeout)
        json_data = response.read()
        return json_data
    finally:
        if response is not None:
            try:
                response.close()
            except:
                pass


def execute(configurations={}, parameters={}, host_name=None):
    """
    Returns a tuple containing the result code and a pre-formatted result label

    Keyword arguments:
    configurations : a mapping of configuration key to value
    parameters : a mapping of script parameter key to value
    host_name : the name of this host where the alert is running

    :type configurations dict
    :type parameters dict
    :type host_name str
    """

    alert_name = parameters['alertName']

    dolphin_pidfile_dir = "/opt/soft/run/dolphinscheduler"

    pid = "0"


    from resource_management.core import sudo

    is_running = True
    pid_file_path = ""
    if alert_name == 'DOLPHIN_MASTER':
        pid_file_path = dolphin_pidfile_dir + "/master-server.pid"
    elif alert_name == 'DOLPHIN_WORKER':
        pid_file_path = dolphin_pidfile_dir + "/worker-server.pid"
    elif alert_name == 'DOLPHIN_ALERT':
        pid_file_path = dolphin_pidfile_dir + "/alert-server.pid"
    elif alert_name == 'DOLPHIN_LOGGER':
        pid_file_path = dolphin_pidfile_dir + "/logger-server.pid"
    elif alert_name == 'DOLPHIN_API':
        pid_file_path = dolphin_pidfile_dir + "/api-server.pid"

    if not pid_file_path or not os.path.isfile(pid_file_path):
        is_running = False

    try:
        pid = int(sudo.read_file(pid_file_path))
    except:
        is_running = False

    try:
        # Kill will not actually kill the process
        # From the doc:
        # If sig is 0, then no signal is sent, but error checking is still
        # performed; this can be used to check for the existence of a
        # process ID or process group ID.
        sudo.kill(pid, 0)
    except OSError:
        is_running = False

    if host_name is None:
        host_name = socket.getfqdn()

    if not is_running:
        result_code = "CRITICAL"
    else:
        result_code = "OK"

    label = "The comment {0} of DOLPHIN_SCHEDULER on {1} is {2}".format(alert_name, host_name, result_code)

    return ((result_code, [label]))

if __name__ == "__main__":
    pass
