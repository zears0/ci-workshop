FROM python:3.6

ENV PROJECTID USERID

# The application's directory will be the working directory
WORKDIR /app

# Copy app's source code to the /app directory
COPY src /app

# Install Node.js dependencies defined in '/app/packages.json'
RUN pip install -r requirements.txt

EXPOSE 5000

# Start the application
CMD ["python", "app.py"]
