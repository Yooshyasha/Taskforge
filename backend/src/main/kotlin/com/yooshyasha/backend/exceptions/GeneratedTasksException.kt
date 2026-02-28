package com.yooshyasha.backend.exceptions

import exceptions.ApiException

class GeneratedTasksNotFound : ApiException("Generated tasks not found", 404)

class TasksZipFailed : ApiException("Tasks zip failed", 400)