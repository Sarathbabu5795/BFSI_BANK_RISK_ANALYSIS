import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiLogFlag,
  DashboardSummary,
  IncidentFlag,
  RiskAssessment,
  TransactionFlag
} from '../models/risk.models';

@Injectable({ providedIn: 'root' })
export class RiskApiService {
  private readonly base = '/api';

  constructor(private http: HttpClient) {}

  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.base}/dashboard/summary`);
  }

  getFlaggedTransactions(rule?: string): Observable<TransactionFlag[]> {
    const url = rule ? `${this.base}/transactions/flagged?rule=${rule}` : `${this.base}/transactions/flagged`;
    return this.http.get<TransactionFlag[]>(url);
  }

  getHighRiskTransactions(): Observable<RiskAssessment[]> {
    return this.http.get<RiskAssessment[]>(`${this.base}/transactions/high-risk`);
  }

  getLabelMismatches(): Observable<RiskAssessment[]> {
    return this.http.get<RiskAssessment[]>(`${this.base}/transactions/label-mismatches`);
  }

  getTopAnomalies(limit = 50): Observable<RiskAssessment[]> {
    return this.http.get<RiskAssessment[]>(`${this.base}/transactions/top-anomalies?limit=${limit}`);
  }

  getSlaBreaches(): Observable<IncidentFlag[]> {
    return this.http.get<IncidentFlag[]>(`${this.base}/incidents/sla-breaches`);
  }

  getStatusDateConflicts(): Observable<IncidentFlag[]> {
    return this.http.get<IncidentFlag[]>(`${this.base}/incidents/status-date-conflicts`);
  }

  getModuleHotspots(limit = 10): Observable<{ category: string; count: number }[]> {
    return this.http.get<{ category: string; count: number }[]>(`${this.base}/incidents/module-hotspots?limit=${limit}`);
  }

  getTopRootCauses(limit = 10): Observable<{ category: string; count: number }[]> {
    return this.http.get<{ category: string; count: number }[]>(`${this.base}/incidents/top-root-causes?limit=${limit}`);
  }

  getSlowApiCalls(): Observable<ApiLogFlag[]> {
    return this.http.get<ApiLogFlag[]>(`${this.base}/api-logs/slow`);
  }

  getApiFailures(): Observable<ApiLogFlag[]> {
    return this.http.get<ApiLogFlag[]>(`${this.base}/api-logs/failures`);
  }

  getTopFailingApis(limit = 10): Observable<{ category: string; count: number }[]> {
    return this.http.get<{ category: string; count: number }[]>(`${this.base}/api-logs/top-failing?limit=${limit}`);
  }

  downloadReportUrl(): string {
    return `${this.base}/export/report.pdf`;
  }
}
