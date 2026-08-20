import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Color, NgxChartsModule, ScaleType } from '@swimlane/ngx-charts';
import { DashboardSummary } from '../../core/models/risk.models';
import { RiskApiService } from '../../core/services/risk-api.service';
import { StatCardComponent } from '../../shared/stat-card/stat-card.component';
import { StateBannerComponent } from '../../shared/state-banner/state-banner.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NgxChartsModule, StatCardComponent, StateBannerComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  loading = true;
  error = false;
  summary: DashboardSummary | null = null;

  flagsByRuleChart: { name: string; value: number }[] = [];
  riskCategoryChart: { name: string; value: number }[] = [];
  incidentSeverityChart: { name: string; value: number }[] = [];
  moduleHotspotsChart: { name: string; value: number }[] = [];

  readonly colorScheme: Color = {
    name: 'bfsi',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#1a56db', '#c0263c', '#b45309', '#12805c', '#6b7488', '#5b8def']
  };

  constructor(private api: RiskApiService) {}

  ngOnInit(): void {
    this.api.getDashboardSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
        this.flagsByRuleChart = summary.flagsByRule.map((c) => ({ name: c.category, value: c.count }));
        this.riskCategoryChart = summary.riskCategoryDistribution.map((c) => ({ name: c.category, value: c.count }));
        this.incidentSeverityChart = summary.incidentSeverityDistribution.map((c) => ({ name: c.category, value: c.count }));
        this.moduleHotspotsChart = summary.moduleHotspots.map((c) => ({ name: c.category, value: c.count }));
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  get reportUrl(): string {
    return this.api.downloadReportUrl();
  }
}
