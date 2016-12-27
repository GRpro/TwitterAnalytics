package kpi.twitter.analysis.utils

import twitter4j.Status

case class PredictedStatus(sentiment: Int, status: Status)
