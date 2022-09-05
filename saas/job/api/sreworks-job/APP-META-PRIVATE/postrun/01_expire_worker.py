import os
import MySQLdb

DB_HOST = os.getenv('DB_HOST')
DB_USER = os.getenv('DB_USER')
DB_PASSWORD = os.getenv('DB_PASSWORD')
DB_PORT = os.getenv('DB_PORT')
DB_NAME = os.getenv("DB_NAME")

job_db = MySQLdb.connect(host=DB_HOST, port=int(DB_PORT), user=DB_USER, passwd=DB_PASSWORD, db=DB_NAME, charset='utf8')
job_cursor = job_db.cursor()
job_cursor.execute("truncate table sreworks_job_worker")
job_db.commit()
job_cursor.close()
job_db.close()
