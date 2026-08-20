import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Color, NgxChartsModule, ScaleType } from '@swimlane/ngx-charts';
import { forkJoin } from 'rxjs';
import { CategoryCount, IncidentFlag } from '../../core/models/risk.models';
import { RiskApiService } from '../../core/services/risk-api.service';
import { StateBannerComponent } from '../../shared/state-banner/state-banner.component';

type ViewMode = 'sla' | 'conflicts';

@Component({
  selector: 'app-incidents',
  standalone: true,
  imports: [CommonModule, NgxChartsModule, StateBannerComponent],
  templateUrl: './incidents.component.html',
  styleUrl: './incidents.component.scss'
})
export class IncidentsComponent implements OnInit {
  loading = true;
  error = false;
  view: ViewMode = 'sla';

  slaBreaches: IncidentFlag[] = [];
  statusDateConflicts: IncidentFlag[] = [];
  moduleHotspots: CategoryCount[] = [];
  topRootCauses: CategoryCount[] = [];

  moduleHotspotsChart: { name: string; value: number }[] = [];
  rootCausesChart: { name: string; value: number }[] = [];

  readonly colorScheme: Color = {
    name: 'bfsi',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#1a56db', '#c0263c', '#b45309', '#12805c', '#6b7488', '#5b8def']
  };

  constructor(private api: RiskApiService) {}

  ngOnInit(): void {
    this.loading = true;
    this.error = false;
    forkJoin({
      slaBreaches: this.api.getSlaBreaches(),
      statusDateConflicts: this.api.getStatusDateConflicts(),
      moduleHotspots: this.api.getModuleHotspots(10),
      topRootCauses: this.api.getTopRootCauses(10)
    }).subscribe({
      next: ({ slaBreaches, statusDateConflicts, moduleHotspots, topRootCauses }) => {
        this.slaBreaches = slaBreaches;
        this.statusDateConflicts = statusDateConflicts;
        this.moduleHotspots = moduleHotspots;
        this.topRootCauses = topRootCauses;
        this.moduleHotspotsChart = moduleHotspots.map((c) => ({ name: c.category, value: c.count }));
        this.rootCausesChart = topRootCauses.map((c) => ({ name: c.category, value: c.count }));
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
