import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Color, NgxChartsModule, ScaleType } from '@swimlane/ngx-charts';
import { forkJoin } from 'rxjs';
import { ApiLogFlag, CategoryCount } from '../../core/models/risk.models';
import { RiskApiService } from '../../core/services/risk-api.service';
import { StateBannerComponent } from '../../shared/state-banner/state-banner.component';

type ViewMode = 'slow' | 'failures';

@Component({
  selector: 'app-api-health',
  standalone: true,
  imports: [CommonModule, NgxChartsModule, StateBannerComponent],
  templateUrl: './api-health.component.html',
  styleUrl: './api-health.component.scss'
})
export class ApiHealthComponent implements OnInit {
  loading = true;
  error = false;
  view: ViewMode = 'slow';

  slowCalls: ApiLogFlag[] = [];
  failures: ApiLogFlag[] = [];
  topFailingApis: CategoryCount[] = [];
  topFailingChart: { name: string; value: number }[] = [];

  readonly colorScheme: Color = {
    name: 'bfsi',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#c0263c', '#b45309', '#1a56db', '#12805c', '#6b7488', '#5b8def']
  };

  constructor(private api: RiskApiService) {}

  ngOnInit(): void {
    this.loading = true;
    this.error = false;
    forkJoin({
      slowCalls: this.api.getSlowApiCalls(),
      failures: this.api.getApiFailures(),
      topFailingApis: this.api.getTopFailingApis(10)
    }).subscribe({
      next: ({ slowCalls, failures, topFailingApis }) => {
        this.slowCalls = slowCalls;
        this.failures = failures;
        this.topFailingApis = topFailingApis;
        this.topFailingChart = topFailingApis.map((c) => ({ name: c.category, value: c.count }));
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  setView(view: ViewMode): void {
    this.view = view;
  }
}
