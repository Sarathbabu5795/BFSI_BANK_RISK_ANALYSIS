import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { RiskAssessment, TransactionFlag } from '../../core/models/risk.models';
import { RiskApiService } from '../../core/services/risk-api.service';
import { StateBannerComponent } from '../../shared/state-banner/state-banner.component';

const RULES = [
  { code: '', label: 'All rules' },
  { code: 'BR-001', label: 'BR-001 Invalid customer relationship' },
  { code: 'BR-002', label: 'BR-002 Negative/zero amount' },
  { code: 'BR-003', label: 'BR-003 Future-dated transaction' },
  { code: 'BR-004', label: 'BR-004 Closed-account activity' },
  { code: 'BR-005', label: 'BR-005 KYC high-value' },
  { code: 'BR-006', label: 'BR-006 Invalid currency' },
  { code: 'DUP-ID', label: 'Duplicate transaction ID' }
];

type ViewMode = 'flagged' | 'high-risk' | 'label-mismatch';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule, StateBannerComponent],
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.scss'
})
export class TransactionsComponent implements OnInit {
  loading = true;
  error = false;

  view: ViewMode = 'flagged';
  rules = RULES;
  selectedRule = '';

  flags: TransactionFlag[] = [];
  highRisk: RiskAssessment[] = [];
  labelMismatches: RiskAssessment[] = [];

  constructor(private api: RiskApiService) {}

  ngOnInit(): void {
    this.loadAll();
  }

  private loadAll(): void {
    this.loading = true;
    this.error = false;
    forkJoin({
      flags: this.api.getFlaggedTransactions(),
      highRisk: this.api.getHighRiskTransactions(),
      labelMismatches: this.api.getLabelMismatches()
    }).subscribe({
      next: ({ flags, highRisk, labelMismatches }) => {
        this.flags = flags;
        this.highRisk = highRisk;
        this.labelMismatches = labelMismatches;
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

  get filteredFlags(): TransactionFlag[] {
    if (!this.selectedRule) return this.flags;
    return this.flags.filter((f) => f.ruleCode === this.selectedRule);
  }
}
