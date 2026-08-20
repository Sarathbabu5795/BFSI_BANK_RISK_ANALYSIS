import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-state-banner',
  standalone: true,
  template: `
    <div class="state-banner" [class]="kind">
      <span class="dot"></span>
      <span>{{ message }}</span>
    </div>
  `,
  styleUrl: './state-banner.component.scss'
})
export class StateBannerComponent {
  @Input() kind: 'loading' | 'error' | 'empty' = 'loading';
  @Input() message = '';
}
