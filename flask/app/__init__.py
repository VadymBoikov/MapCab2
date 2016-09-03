from flask import Flask
app = Flask(__name__)

import logging
log = logging.getLogger('werkzeug')
log.setLevel(logging.INFO)

from app import views

