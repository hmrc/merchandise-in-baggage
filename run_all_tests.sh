#!/usr/bin/env bash

sbt clean compile scalafmtAll coverage test dependencyUpdates coverageReport
