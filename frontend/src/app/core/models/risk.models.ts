export interface CategoryCount {
  category: string;
  count: number;
}

export interface DashboardSummary {
  totalCustomers: number;
  totalAccounts: number;
  totalTransactions: number;
  totalIncidents: number;
  totalApiLogs: number;
  flaggedTransactionCount: number;
  highRiskTransactionCount: number;
  labelMismatchCount: number;
  averageRiskScore: number;
  slaBreachCount: number;
  statusDateConflictCount: number;
  slowApiCount: number;
  serverFailureCount: number;
  flagsByRule: CategoryCount[];
  riskCategoryDistribution: CategoryCount[];
  incidentSeverityDistribution: CategoryCount[];
  moduleHotspots: CategoryCount[];
}

export interface Transaction {
  transactionId: string;
  accountId: string;
  customerId: string;
  transactionDatetime: string;
  transactionType: string;
  transactionChannel: string;
  transactionAmount: number;
  currency: string;
  beneficiaryId: string;
  sourceLocation: string;
  destinationLocation: string;
  deviceId: string;
  ipAddress: string;
  transactionStatus: string;
  failureReason: string;
  settlementStatus: string;
  fraudFlag: string;
  riskScore: number;
}

export interface TransactionFlag {
  ruleCode: string;
  ruleName: string;
  transaction: Transaction;
  reason: string;
}

export interface RiskAssessment {
  transaction: Transaction;
  providedRiskScore: number;
  computedAnomalyScore: number;
  expectedHighRisk: boolean;
  fraudFlagged: boolean;
  labelMismatch: boolean;
  reason: string;
}

export interface Incident {
  incidentId: string;
  incidentTitle: string;
  applicationModule: string;
  severity: string;
  priority: string;
  reportedDatetime: string;
  environment: string;
  incidentStatus: string;
  assignedTeam: string;
  assignedEngineer: string;
  rootCause: string;
  resolutionSummary: string;
  resolvedDatetime: string | null;
  slaHours: number;
  slaBreached: string;
  relatedTransactionId: string;
  relatedReleaseId: string;
}

export interface IncidentFlag {
  ruleCode: string;
  ruleName: string;
  incident: Incident;
  reason: string;
}

export interface ApiLog {
  logId: string;
  timestamp: string;
  apiName: string;
  endpoint: string;
  requestMethod: string;
  responseCode: number;
  responseTimeMs: number;
  requestSizeBytes: number;
  responseSizeBytes: number;
  serverName: string;
  environment: string;
  errorCode: string;
  timeoutFlag: string;
  transactionId: string;
}

export interface ApiLogFlag {
  ruleCode: string;
  ruleName: string;
  apiLog: ApiLog;
  reason: string;
}
