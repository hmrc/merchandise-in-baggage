#!/usr/bin/env bash

sbt clean scalafmtAll scalastyleAll compile coverage test coverageOff dependencyUpdates coverageReport
