FROM ${SW_PYTHON3_IMAGE}
WORKDIR /root/test/
COPY . .
RUN pip install kubernetes
CMD ["diagnosis.py"]
ENTRYPOINT ["python3"]